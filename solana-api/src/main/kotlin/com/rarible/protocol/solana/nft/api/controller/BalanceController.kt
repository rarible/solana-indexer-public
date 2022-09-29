package com.rarible.protocol.solana.nft.api.controller

import com.rarible.protocol.solana.api.controller.BalanceControllerApi
import com.rarible.protocol.solana.common.continuation.BalanceContinuation
import com.rarible.protocol.solana.common.continuation.DateIdContinuation
import com.rarible.protocol.solana.common.continuation.Paging
import com.rarible.protocol.solana.common.converter.BalanceWithMetaConverter
import com.rarible.protocol.solana.common.model.BalanceWithMeta
import com.rarible.protocol.solana.dto.BalanceDto
import com.rarible.protocol.solana.dto.BalanceIdsDto
import com.rarible.protocol.solana.dto.BalancesDto
import com.rarible.protocol.solana.nft.api.service.BalanceApiService
import com.rarible.protocol.union.dto.continuation.page.PageSize
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class BalanceController(
    private val balanceApiService: BalanceApiService,
) : BalanceControllerApi {

    override suspend fun getBalanceByMintAndOwner(mint: String, owner: String): ResponseEntity<BalanceDto> {
        val balance = balanceApiService.getBalanceByMintAndOwner(mint, owner)
        return ResponseEntity.ok(BalanceWithMetaConverter.convert(balance))
    }

    override suspend fun getBalanceByOwner(
        owner: String,
        continuation: String?,
        size: Int?
    ): ResponseEntity<BalancesDto> {
        val safeSize = PageSize.BALANCE.limit(size)
        val balances = balanceApiService.getBalanceByOwner(
            owner,
            DateIdContinuation.parse(continuation),
            safeSize
        ).toList()

        val dto = toSlice(balances, safeSize)
        return ResponseEntity.ok(dto)
    }

    override suspend fun getBalancesAll(
        continuation: String?,
        size: Int?,
        showDeleted: Boolean?
    ): ResponseEntity<BalancesDto> {
        val safeSize = PageSize.BALANCE.limit(size)
        val balances = balanceApiService.getBalancesAll(
            showDeleted,
            DateIdContinuation.parse(continuation),
            safeSize
        ).toList()

        val dto = toSlice(balances, safeSize)
        return ResponseEntity.ok(dto)
    }

    override suspend fun getBalancesByMintAndOwner(
        mint: String,
        owner: String
    ): ResponseEntity<BalancesDto> {
        val balances = balanceApiService.getBalancesByMintAndOwner(
            mint = mint,
            owner = owner
        ).map { BalanceWithMetaConverter.convert(it) }.toList()
        return ResponseEntity.ok(BalancesDto(balances, null))
    }

    override suspend fun getBalanceByMintAndOwnerBatch(balanceIdsDto: BalanceIdsDto): ResponseEntity<BalancesDto> {
        val mintOwnerList = balanceIdsDto.ids.map {
            val parts = it.split(":")

            if (parts.size != 2) return ResponseEntity.badRequest().build()

            parts[0] to parts[1]
        }

        val balances = coroutineScope {
            mintOwnerList.map { (mint, owner) ->
                async {
                    balanceApiService.getBalancesByMintAndOwner(
                        mint = mint,
                        owner = owner
                    ).map { BalanceWithMetaConverter.convert(it) }.toList()
                }
            }.awaitAll().flatten()
        }

        return ResponseEntity.ok(BalancesDto(balances, null))
    }

    override suspend fun getBalanceByMint(
        mint: String,
        continuation: String?,
        size: Int?
    ): ResponseEntity<BalancesDto> {
        val safeSize = PageSize.BALANCE.limit(size)
        val balances = balanceApiService.getBalanceByMint(
            mint,
            DateIdContinuation.parse(continuation),
            safeSize
        ).toList()

        val dto = toSlice(balances, safeSize)
        return ResponseEntity.ok(dto)
    }

    private fun toSlice(balances: List<BalanceWithMeta>, size: Int): BalancesDto {
        val dto = balances.map { BalanceWithMetaConverter.convert(it) }
        val continuationFactory = BalanceContinuation.ByLastUpdatedAndId

        val slice = Paging(continuationFactory, dto).getSlice(size)
        return BalancesDto(slice.entities, slice.continuation)
    }

}
