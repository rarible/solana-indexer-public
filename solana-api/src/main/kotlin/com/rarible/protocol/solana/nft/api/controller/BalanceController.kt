package com.rarible.protocol.solana.nft.api.controller

import com.rarible.protocol.solana.api.controller.BalanceControllerApi
import com.rarible.protocol.solana.common.continuation.BalanceContinuation
import com.rarible.protocol.solana.common.continuation.DateIdContinuation
import com.rarible.protocol.solana.common.continuation.Paging
import com.rarible.protocol.solana.common.converter.BalanceWithMetaConverter
import com.rarible.protocol.solana.common.model.BalanceWithMeta
import com.rarible.protocol.solana.dto.BalanceDto
import com.rarible.protocol.solana.dto.BalancesDto
import com.rarible.protocol.solana.nft.api.service.BalanceApiService
import com.rarible.protocol.union.dto.continuation.page.PageSize
import kotlinx.coroutines.flow.toList
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class BalanceController(
    private val balanceApiService: BalanceApiService,
) : BalanceControllerApi {

    override suspend fun getBalanceByAccount(accountAddress: String): ResponseEntity<BalanceDto> {
        val balanceWithMeta = balanceApiService.getBalanceWithMetaByAccountAddress(accountAddress)
        return ResponseEntity.ok(BalanceWithMetaConverter.convert(balanceWithMeta))
    }

    override suspend fun getBalanceByOwner(
        owner: String,
        continuation: String?,
        size: Int?
    ): ResponseEntity<BalancesDto> {
        val safeSize = PageSize.BALANCE.limit(size)
        val balancesWithMeta = balanceApiService.getBalanceWithMetaByOwner(
            owner,
            DateIdContinuation.parse(continuation),
            safeSize
        ).toList()

        val dto = toSlice(balancesWithMeta, safeSize)
        return ResponseEntity.ok(dto)
    }

    override suspend fun getBalanceByMint(
        mint: String,
        continuation: String?,
        size: Int?
    ): ResponseEntity<BalancesDto> {
        val safeSize = PageSize.BALANCE.limit(size)
        val balancesWithMeta = balanceApiService.getBalanceWithMetaByMint(
            mint,
            DateIdContinuation.parse(continuation),
            safeSize
        ).toList()

        val dto = toSlice(balancesWithMeta, safeSize)
        return ResponseEntity.ok(dto)
    }

    private fun toSlice(balances: List<BalanceWithMeta>, size: Int): BalancesDto {
        val dto = balances.map { BalanceWithMetaConverter.convert(it) }
        val continuationFactory = BalanceContinuation.ByLastUpdatedAndId

        val slice = Paging(continuationFactory, dto).getSlice(size)
        return BalancesDto(slice.entities, slice.continuation)
    }

}
