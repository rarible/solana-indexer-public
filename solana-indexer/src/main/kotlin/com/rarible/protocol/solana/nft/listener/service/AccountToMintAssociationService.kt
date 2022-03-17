package com.rarible.protocol.solana.nft.listener.service

import com.rarible.core.apm.CaptureSpan
import com.rarible.core.apm.SpanType
import com.rarible.core.apm.withSpan
import com.rarible.protocol.solana.nft.listener.repository.BalanceLogRepository
import com.rarible.protocol.solana.nft.listener.service.currency.CurrencyTokenReader
import io.lettuce.core.api.reactive.RedisReactiveCommands
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
@CaptureSpan(SpanType.APP)
class AccountToMintAssociationService(
    private val balanceLogRepository: BalanceLogRepository,
    private val redis: RedisReactiveCommands<String, String>,
    currencyTokenReader: CurrencyTokenReader
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    private val currencyTokens = currencyTokenReader.readCurrencyTokens().tokens.map { it.address }

    suspend fun getMintByAccount(account: String): String? = getMintsByAccounts(listOf(account))[account]

    suspend fun getMintsByAccounts(accounts: Collection<String>): Map<String, String> {
        val fromCache = getCachedMintsByAccounts(accounts)
        if (fromCache.size == accounts.size) {
            logger.info("Account to mint cache hit: {} of {}", accounts.size, fromCache.size)
            return fromCache
        }

        val notCached = accounts.filterNot { fromCache.containsKey(it) }
        val fromDb = HashMap<String, String>()

        balanceLogRepository.findBalanceInitializationRecords(notCached)
            .collect { fromDb[it.balanceAccount] = it.mint }

        saveAccountToMintMapping(fromDb)

        logger.info("Account to mint cache hit: {} of {}, {} found in DB", accounts.size, fromCache.size, fromDb.size)
        return fromCache + fromDb
    }

    suspend fun saveAccountToMintMapping(accountToMints: Map<String, String>) {
        withSpan("AccountToMintAssociationService#saveBalanceTokens", SpanType.CACHE) {
            redis.mset(accountToMints).awaitFirstOrNull()
        }
    }

    suspend fun isCurrencyToken(mint: String): Boolean = currencyTokens.contains(mint)

    private suspend fun getCachedMintsByAccounts(accounts: Collection<String>): Map<String, String> =
        withSpan("AccountToMintAssociationService#getCachedMintsByAccounts", SpanType.CACHE) {
            redis.mget(*accounts.toTypedArray())
                .filter { it.hasValue() }
                .collectMap({ it.key }, { it.value })
                .awaitFirst()
        }
}
