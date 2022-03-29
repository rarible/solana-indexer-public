package com.rarible.protocol.solana.nft.api.service

import com.rarible.protocol.solana.common.continuation.DateIdContinuation
import com.rarible.protocol.solana.common.meta.TokenMetaService
import com.rarible.protocol.solana.common.model.BalanceWithMeta
import com.rarible.protocol.solana.common.repository.BalanceRepository
import com.rarible.protocol.solana.nft.api.exceptions.EntityNotFoundApiException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.stereotype.Component

@Component
class BalanceApiService(
    private val balanceRepository: BalanceRepository,
    private val tokenMetaService: TokenMetaService
) {

    suspend fun getBalanceWithMetaByAccountAddress(accountAddress: String): BalanceWithMeta {
        val balance = (balanceRepository.findByAccount(accountAddress)
            ?: throw EntityNotFoundApiException("Balance", accountAddress))
        return tokenMetaService.extendWithAvailableMeta(balance)
    }

    fun getBalanceWithMetaByOwner(owner: String, continuation: DateIdContinuation?, limit: Int): Flow<BalanceWithMeta> =
        balanceRepository.findByOwner(
            owner,
            continuation,
            limit
        ).map { tokenMetaService.extendWithAvailableMeta(it) }

    fun getBalanceWithMetaByMint(mint: String, continuation: DateIdContinuation?, limit: Int): Flow<BalanceWithMeta> =
        balanceRepository.findByMint(
            mint,
            continuation,
            limit
        ).map { tokenMetaService.extendWithAvailableMeta(it) }
}
