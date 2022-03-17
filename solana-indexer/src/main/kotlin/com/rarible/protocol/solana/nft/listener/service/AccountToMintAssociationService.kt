package com.rarible.protocol.solana.nft.listener.service

import com.rarible.core.apm.CaptureSpan
import com.rarible.core.apm.SpanType
import com.rarible.protocol.solana.nft.listener.repository.BalanceLogRepository
import com.rarible.protocol.solana.nft.listener.service.currency.CurrencyTokenReader
import io.lettuce.core.api.reactive.RedisReactiveCommands
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Component

@Component
@CaptureSpan(SpanType.CACHE)
class AccountToMintAssociationService(
    private val balanceLogRepository: BalanceLogRepository,
    private val redis: RedisReactiveCommands<String, String>,
    currencyTokenReader: CurrencyTokenReader
) {

    private val currencyTokens = currencyTokenReader.readCurrencyTokens().tokens.map { it.address }

    suspend fun getMintsByAccounts(accounts: List<String>): Map<String, String> {
        val fromCache = getCachedMintsByAccounts(accounts)
        if (fromCache.size == accounts.size) {
            return fromCache
        }

        val notCached = accounts.filterNot { fromCache.containsKey(it) }
        val fromDb = HashMap<String, String>()

        balanceLogRepository.findBalanceInitializationRecords(notCached)
            .collect { fromDb[it.balanceAccount] = it.mint }

        saveCachedAccountToMint(fromDb)

        return fromCache + fromDb
    }

    suspend fun saveBalanceTokens(accountToMints: Map<String, String>) {
        saveCachedAccountToMint(accountToMints)
    }

    suspend fun isCurrencyToken(mint: String): Boolean = currencyTokens.contains(mint)

    private suspend fun saveCachedAccountToMint(accountToMint: Map<String, String>) {
        redis.mset(accountToMint).awaitFirstOrNull()
    }

    private suspend fun getCachedMintsByAccounts(accounts: Collection<String>): Map<String, String> =
        redis.mget(*accounts.toTypedArray())
            .filter { it.value != null }
            .collectMap({ it.key }, { it.value })
            .awaitFirst()
}
