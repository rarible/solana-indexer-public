package com.rarible.protocol.solana.nft.listener.service

import com.rarible.protocol.solana.nft.listener.repository.BalanceLogRepository
import com.rarible.protocol.solana.nft.listener.util.CurrencyTokenReader
import org.springframework.stereotype.Component

@Component
class BalanceTokenService(
    private val balanceTokenCacheService: BalanceTokenCacheService,
    private val balanceLogRepository: BalanceLogRepository,
    currencyTokenReader: CurrencyTokenReader
) {

    private val currencyTokens = currencyTokenReader.readCurrencyTokens().tokens.map { it.address }

    suspend fun getBalanceTokens(balances: List<String>): Map<String, String> {
        val fromCache = balanceTokenCacheService.getBalanceTokens(balances)
        if (fromCache.size == balances.size) {
            return fromCache
        }

        val notCached = balances.filterNot { fromCache.containsKey(it) }
        val fromDb = HashMap<String, String>()

        balanceLogRepository.findBalanceInitializationRecords(notCached)
            .collect { fromDb[it.balanceAccount] = it.mint }

        balanceTokenCacheService.saveBalanceTokens(fromDb)

        return fromCache + fromDb
    }

    suspend fun saveBalanceTokens(balanceToToken: Map<String, String>) {
        balanceTokenCacheService.saveBalanceTokens(balanceToToken)
    }

    suspend fun isCurrencyToken(mint: String): Boolean {
        return currencyTokens.contains(mint)
    }

}