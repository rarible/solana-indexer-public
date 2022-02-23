package com.rarible.protocol.solana.nft.api.controller

import com.rarible.protocol.solana.common.converter.BalanceWithMetaConverter
import com.rarible.protocol.solana.nft.api.service.BalanceApiService
import com.rarible.solana.protocol.api.controller.BalanceControllerApi
import com.rarible.solana.protocol.dto.BalanceDto
import com.rarible.solana.protocol.dto.BalancesDto
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

    // TODO: add continuation/size limit parameters.
    override suspend fun getBalanceByOwner(owner: String): ResponseEntity<BalancesDto> {
        val balancesWithMeta = balanceApiService.getBalanceWithMetaByOwner(owner).toList()
        return ResponseEntity.ok(BalanceWithMetaConverter.convert(balancesWithMeta))
    }

    override suspend fun getBalanceByMint(mint: String): ResponseEntity<BalancesDto> {
        val balancesWithMeta = balanceApiService.getBalanceWithMetaByMint(mint).toList()
        return ResponseEntity.ok(BalanceWithMetaConverter.convert(balancesWithMeta))
    }
}
