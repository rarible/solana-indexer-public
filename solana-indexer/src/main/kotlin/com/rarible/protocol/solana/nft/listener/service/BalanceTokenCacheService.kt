package com.rarible.protocol.solana.nft.listener.service

import com.rarible.core.apm.CaptureSpan
import com.rarible.core.apm.SpanType
import io.lettuce.core.api.reactive.RedisReactiveCommands
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Component

@Component
@CaptureSpan(SpanType.CACHE)
class BalanceTokenCacheService(
    private val redis: RedisReactiveCommands<String, String>
) {

    suspend fun saveBalanceTokens(balanceToToken: Map<String, String>) {
        redis.mset(balanceToToken).awaitFirstOrNull()
    }

    suspend fun getBalanceTokens(balances: Collection<String>): Map<String, String> {
        return redis.mget(*balances.toTypedArray())
            .filter { it.value != null }
            .collectMap({ it.key }, { it.value })
            .awaitFirst()
    }

}