package com.rarible.protocol.solana.nft.listener.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.cloudyrock.spring.v5.EnableMongock
import com.rarible.blockchain.scanner.configuration.KafkaProperties
import com.rarible.blockchain.scanner.publisher.LogRecordEventPublisher
import com.rarible.blockchain.scanner.solana.EnableSolanaScanner
import com.rarible.blockchain.scanner.solana.client.SolanaHttpRpcApi
import com.rarible.blockchain.scanner.solana.configuration.SolanaBlockchainScannerProperties
import com.rarible.core.application.ApplicationEnvironmentInfo
import com.rarible.core.lockredis.EnableRaribleRedisLock
import com.rarible.protocol.solana.common.configuration.FeatureFlags
import com.rarible.protocol.solana.common.configuration.SolanaIndexerProperties
import com.rarible.protocol.solana.common.configuration.TokenFilterType
import com.rarible.protocol.solana.nft.listener.block.cache.BlockCacheRepository
import com.rarible.protocol.solana.nft.listener.block.cache.SolanaCacheApi
import com.rarible.protocol.solana.nft.listener.consumer.KafkaEntityEventConsumer
import com.rarible.protocol.solana.nft.listener.consumer.LogRecordEventListener
import com.rarible.protocol.solana.nft.listener.service.subscribers.SubscriberGroup
import com.rarible.protocol.solana.nft.listener.service.subscribers.filter.NftTokenReader
import com.rarible.protocol.solana.nft.listener.service.subscribers.filter.SolanaBlackListTokenFilter
import com.rarible.protocol.solana.nft.listener.service.subscribers.filter.SolanaTokenFilter
import com.rarible.protocol.solana.nft.listener.service.subscribers.filter.SolanaWhiteListTokenFilter
import io.lettuce.core.api.reactive.RedisReactiveCommands
import io.micrometer.core.instrument.MeterRegistry
import kotlinx.coroutines.runBlocking
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
@EnableSolanaScanner
@EnableRaribleRedisLock
@EnableMongock
class BlockchainScannerConfiguration(
    private val solanaIndexerProperties: SolanaIndexerProperties,
    private val meterRegistry: MeterRegistry,
    private val applicationEnvironmentInfo: ApplicationEnvironmentInfo
) {

    private val WHITELIST_FILES = listOf(
        "degenape",
        "degenerate_ape_kindergarten",
        "degeneratetrashpandas"
    )

    private val BLACKLIST_FILES = emptyList<String>()

    @Bean
    fun solanaApi(
        repository: BlockCacheRepository,
        properties: SolanaBlockchainScannerProperties,
        mapper: ObjectMapper,
        meterRegistry: MeterRegistry
    ) = SolanaCacheApi(
        repository,
        SolanaHttpRpcApi(properties.rpcApiUrls, properties.rpcApiTimeout),
        mapper,
        meterRegistry
    )

    @Bean
    fun entityEventConsumer(
        logRecordEventListener: List<LogRecordEventListener>,
        solanaBlockchainScannerProperties: SolanaBlockchainScannerProperties,
        publisher: LogRecordEventPublisher
    ): KafkaEntityEventConsumer {
        // TODO: will be reworked on blockchain scanner side.
        runBlocking {
            SubscriberGroup.values().forEach {
                publisher.prepareGroup(it.id)
            }
        }
        return KafkaEntityEventConsumer(
            properties = KafkaProperties(
                brokerReplicaSet = solanaIndexerProperties.kafkaReplicaSet,
            ),
            meterRegistry = meterRegistry,
            applicationEnvironmentInfo = applicationEnvironmentInfo,
            solanaBlockchainScannerProperties = solanaBlockchainScannerProperties
        ).apply { start(logRecordEventListener) }
    }

    // TODO: until CHARLIE-172 is fixed, let's set a timeout for the Redis.
    @Bean
    fun setRedisTimeout(
        redis: RedisReactiveCommands<String, String>
    ): CommandLineRunner = CommandLineRunner {
        redis.setTimeout(Duration.ofSeconds(1))
    }

    @Bean
    fun tokenFilter(featureFlags: FeatureFlags): SolanaTokenFilter {
        return when (featureFlags.tokenFilter) {
            TokenFilterType.NONE -> {
                object : SolanaTokenFilter {
                    override fun isAcceptableToken(mint: String): Boolean = true
                }
            }
            TokenFilterType.WHITELIST -> {
                val tokens = NftTokenReader("/whitelist").readTokens(WHITELIST_FILES)
                SolanaWhiteListTokenFilter(tokens)
            }
            TokenFilterType.BLACKLIST -> {
                val tokens = NftTokenReader("/blacklist").readTokens(BLACKLIST_FILES) + featureFlags.blacklistTokens
                SolanaBlackListTokenFilter(tokens)
            }
        }

    }
}
