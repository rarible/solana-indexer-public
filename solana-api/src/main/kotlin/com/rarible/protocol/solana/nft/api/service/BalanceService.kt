package com.rarible.protocol.solana.nft.api.service

import com.rarible.protocol.solana.common.model.Balance
import com.rarible.protocol.solana.common.repository.BalanceRepository
import com.rarible.protocol.solana.nft.api.exceptions.EntityNotFoundApiException
import kotlinx.coroutines.flow.Flow
import org.springframework.stereotype.Component

@Component
class BalanceService(
    private val balanceRepository: BalanceRepository
) {
    suspend fun getBalance(accountAddress: String): Balance =
        balanceRepository.findByAccount(accountAddress)
            ?: throw EntityNotFoundApiException("Balance", accountAddress)

    fun getBalanceByOwner(owner: String): Flow<Balance> =
        balanceRepository.findByOwner(owner)

    fun getBalanceByMint(mint: String): Flow<Balance> =
        balanceRepository.findByMint(mint)
}
