package com.rarible.protocol.solana.nft.api.service

import com.rarible.protocol.solana.common.model.Balance
import com.rarible.protocol.solana.common.repository.BalanceRepository
import com.rarible.protocol.solana.nft.api.exceptions.EntityNotFoundApiException
import org.springframework.stereotype.Component

@Component
class BalanceService(
    private val balanceRepository: BalanceRepository
) {
    suspend fun getBalance(accountAddress: String): Balance =
        balanceRepository.findByAccount(accountAddress)
            ?: throw EntityNotFoundApiException("Balance", accountAddress)
}
