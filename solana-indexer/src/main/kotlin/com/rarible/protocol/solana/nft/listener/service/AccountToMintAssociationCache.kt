package com.rarible.protocol.solana.nft.listener.service

import com.rarible.core.apm.SpanType
import com.rarible.core.apm.withSpan
import io.lettuce.core.api.reactive.RedisReactiveCommands
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.slf4j.LoggerFactory

open class AccountToMintAssociationCache(
    private val redis: RedisReactiveCommands<String, String>
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    suspend fun getMintsByAccounts(accounts: Collection<String>): Map<String, String> {
        if (accounts.isEmpty()) {
            return emptyMap()
        }

        return try {
            withSpan("AccountToMintAssociationCache.get", type = SpanType.CACHE) {
                redis.mget(*accounts.toTypedArray())
                    .filter { it.hasValue() }
                    .collectMap({ it.key }, { it.value })
                    .awaitFirst()
            }
        } catch (e: Exception) {
            logger.error("Redis error: cannot get account to mint mapping", e)
            emptyMap()
        }
    }

    suspend fun saveMintsByAccounts(accountToMints: Map<String, String>) {
        if (accountToMints.isEmpty()) {
            return
        }

        try {
            withSpan("AccountToMintAssociationCache.save", type = SpanType.CACHE) {
                redis.mset(accountToMints).awaitFirstOrNull()
            }
        } catch (e: Exception) {
            logger.error("Redis error: cannot set account to mint mapping", e)
        }
    }
}
