package com.rarible.protocol.solana.nft.listener.configuration

import com.rarible.core.lockredis.EnableRaribleRedisLock
import com.rarible.protocol.solana.common.configuration.SOLANA_INDEXER_FEATURE_FLAGS
import com.rarible.protocol.solana.nft.listener.service.AccountToMintAssociationCache
import io.lettuce.core.api.reactive.RedisReactiveCommands
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@ConditionalOnProperty(
    prefix = SOLANA_INDEXER_FEATURE_FLAGS,
    name = ["enableAccountToMintAssociationCache"],
    havingValue = "true",
    matchIfMissing = false
)
@Configuration
@EnableRaribleRedisLock
class AccountToMintAssociationConfiguration {
    @Bean
    fun accountToMintAssociationCache(
        redis: RedisReactiveCommands<String, String>
    ): AccountToMintAssociationCache = AccountToMintAssociationCache(redis)

    // TODO: until CHARLIE-172 is fixed, let's set a timeout for the Redis.
    @Bean
    fun setRedisTimeout(
        redis: RedisReactiveCommands<String, String>
    ): CommandLineRunner = CommandLineRunner {
        redis.setTimeout(Duration.ofSeconds(1))
    }
}