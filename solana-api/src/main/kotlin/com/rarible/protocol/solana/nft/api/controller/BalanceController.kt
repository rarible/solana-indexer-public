package com.rarible.protocol.solana.nft.api.controller

import com.rarible.protocol.solana.common.converter.BalanceConverter
import com.rarible.protocol.solana.nft.api.service.BalanceService
import com.rarible.solana.protocol.api.controller.BalanceControllerApi
import com.rarible.solana.protocol.dto.BalanceDto
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class BalanceController(
    private val balanceService: BalanceService
) : BalanceControllerApi {
    override suspend fun getBalanceByAccount(accountAddress: String): ResponseEntity<BalanceDto> {
        val balance = balanceService.getBalance(accountAddress)
        return ResponseEntity.ok(BalanceConverter.convert(balance))
    }
}
