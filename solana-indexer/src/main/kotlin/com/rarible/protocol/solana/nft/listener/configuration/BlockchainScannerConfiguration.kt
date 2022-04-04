package com.rarible.protocol.solana.nft.listener.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.cloudyrock.spring.v5.EnableMongock
import com.rarible.blockchain.scanner.configuration.KafkaProperties
import com.rarible.blockchain.scanner.publisher.LogRecordEventPublisher
import com.rarible.blockchain.scanner.solana.EnableSolanaScanner
import com.rarible.blockchain.scanner.solana.client.SolanaHttpRpcApi
import com.rarible.blockchain.scanner.solana.configuration.SolanaBlockchainScannerProperties
import com.rarible.core.application.ApplicationEnvironmentInfo
import com.rarible.core.entity.reducer.service.StreamFullReduceService
import com.rarible.protocol.solana.common.configuration.FeatureFlags
import com.rarible.protocol.solana.common.configuration.SolanaIndexerProperties
import com.rarible.protocol.solana.common.configuration.TokenFilterType
import com.rarible.protocol.solana.common.records.SubscriberGroup
import com.rarible.protocol.solana.nft.listener.block.cache.BlockCacheRepository
import com.rarible.protocol.solana.nft.listener.block.cache.SolanaCacheApi
import com.rarible.protocol.solana.nft.listener.consumer.KafkaEntityEventConsumer
import com.rarible.protocol.solana.nft.listener.consumer.LogRecordEventListener
import com.rarible.protocol.solana.nft.listener.service.balance.BalanceIdService
import com.rarible.protocol.solana.nft.listener.service.balance.BalanceReducer
import com.rarible.protocol.solana.nft.listener.service.balance.BalanceTemplateProvider
import com.rarible.protocol.solana.nft.listener.service.balance.BalanceUpdateService
import com.rarible.protocol.solana.nft.listener.service.order.OrderIdService
import com.rarible.protocol.solana.nft.listener.service.order.OrderReducer
import com.rarible.protocol.solana.nft.listener.service.order.OrderTemplateProvider
import com.rarible.protocol.solana.nft.listener.service.order.OrderUpdateService
import com.rarible.protocol.solana.nft.listener.service.subscribers.filter.NftTokenReader
import com.rarible.protocol.solana.nft.listener.service.subscribers.filter.SolanaBlackListTokenFilter
import com.rarible.protocol.solana.nft.listener.service.subscribers.filter.SolanaTokenFilter
import com.rarible.protocol.solana.nft.listener.service.subscribers.filter.SolanaWhiteListTokenFilter
import com.rarible.protocol.solana.nft.listener.service.token.TokenIdService
import com.rarible.protocol.solana.nft.listener.service.token.TokenReducer
import com.rarible.protocol.solana.nft.listener.service.token.TokenTemplateProvider
import com.rarible.protocol.solana.nft.listener.service.token.TokenUpdateService
import com.rarible.protocol.solana.nft.listener.task.BalanceStreamFullReduceService
import com.rarible.protocol.solana.nft.listener.task.TokenStreamFullReduceService
import io.micrometer.core.instrument.MeterRegistry
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableSolanaScanner
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
    ) = if (solanaIndexerProperties.featureFlags.enableCacheApi) {
        logger.info("Using SolanaCacheApi")
        SolanaCacheApi(
            repository,
            SolanaHttpRpcApi(properties.rpcApiUrls, properties.rpcApiTimeout),
            mapper,
            meterRegistry
        )
    } else {
        logger.info("Using SolanaHttpRpcApi")
        SolanaHttpRpcApi(properties.rpcApiUrls, properties.rpcApiTimeout)
    }

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

    @Bean
    fun balanceStreamReducer(
        balanceUpdateService: BalanceUpdateService,
        balanceIdService: BalanceIdService,
        balanceTemplateProvider: BalanceTemplateProvider,
        balanceReducer: BalanceReducer
    ) = BalanceStreamFullReduceService(
        balanceUpdateService,
        balanceIdService,
        balanceTemplateProvider,
        balanceReducer
    )

    @Bean
    fun tokenStreamReducer(
        tokenUpdateService: TokenUpdateService,
        tokenIdService: TokenIdService,
        tokenTemplateProvider: TokenTemplateProvider,
        tokenReducer: TokenReducer
    ) = TokenStreamFullReduceService(
        tokenUpdateService,
        tokenIdService,
        tokenTemplateProvider,
        tokenReducer
    )

    @Bean
    fun orderStreamReducer(
        orderUpdateService: OrderUpdateService,
        orderIdService: OrderIdService,
        orderTemplateProvider: OrderTemplateProvider,
        orderReducer: OrderReducer
    ) = StreamFullReduceService(
        orderUpdateService,
        orderIdService,
        orderTemplateProvider,
        orderReducer
    )

    private companion object {
        val logger: Logger = LoggerFactory.getLogger(BlockchainScannerConfiguration::class.java)
    }
}