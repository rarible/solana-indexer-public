package com.rarible.protocol.solana.nft.listener.service

import com.rarible.core.apm.CaptureSpan
import com.rarible.core.apm.SpanType
import com.rarible.protocol.solana.common.configuration.FeatureFlags
import io.lettuce.core.api.reactive.RedisReactiveCommands
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
@CaptureSpan(type = SpanType.CACHE)
class AccountToMintAssociationCache(
    private val redis: RedisReactiveCommands<String, String>,
    private val featureFlags: FeatureFlags
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    suspend fun getMintsByAccounts(accounts: Collection<String>): Map<String, String> {
        if (accounts.isEmpty() || !featureFlags.enableAccountToMintAssociationCache) {
            return emptyMap()
        }

        return try {
            redis.mget(*accounts.toTypedArray())
                .filter { it.hasValue() }
                .collectMap({ it.key }, { it.value })
                .awaitFirst()
        } catch (e: Exception) {
            logger.error("Redis error: cannot get account to mint mapping", e)
            emptyMap()
        }
    }

    suspend fun saveMintsByAccounts(accountToMints: Map<String, String>) {
        if (accountToMints.isEmpty() || !featureFlags.enableAccountToMintAssociationCache) {
            return
        }

        try {
            redis.mset(accountToMints).awaitFirstOrNull()
        } catch (e: Exception) {
            logger.error("Redis error: cannot set account to mint mapping", e)
        }
    }
}
