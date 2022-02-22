package com.rarible.protocol.solana.nft.api.controller

import com.rarible.protocol.solana.common.converter.BalanceConverter
import com.rarible.protocol.solana.common.meta.TokenMetaService
import com.rarible.protocol.solana.nft.api.service.BalanceService
import com.rarible.solana.protocol.api.controller.BalanceControllerApi
import com.rarible.solana.protocol.dto.BalanceDto
import com.rarible.solana.protocol.dto.BalancesDto
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class BalanceApiService(
    private val balanceService: BalanceService,
    private val tokenMetaService: TokenMetaService
) : BalanceControllerApi {
    override suspend fun getBalanceByAccount(accountAddress: String): ResponseEntity<BalanceDto> {
        val balance = balanceService.getBalance(accountAddress)
        val balanceWithMeta = tokenMetaService.extendWithAvailableMeta(balance)

        return ResponseEntity.ok(BalanceConverter.convert(balanceWithMeta))
    }

    // TODO: add continuation/size limit parameters.
    override suspend fun getBalanceByOwner(owner: String): ResponseEntity<BalancesDto> {
        val balancesWithMeta = balanceService.getBalanceByOwner(owner)
            .map { tokenMetaService.extendWithAvailableMeta(it) }
            .toList()

        return ResponseEntity.ok(BalanceConverter.convert(balancesWithMeta))
    }

    override suspend fun getBalanceByMint(mint: String): ResponseEntity<BalancesDto> {
        val balancesWithMeta = balanceService.getBalanceByMint(mint)
            .map { tokenMetaService.extendWithAvailableMeta(it) }
            .toList()

        return ResponseEntity.ok(BalanceConverter.convert(balancesWithMeta))
    }
}
