package com.rarible.protocol.solana.nft.listener.configuration

import com.github.cloudyrock.spring.v5.EnableMongock
import com.rarible.blockchain.scanner.configuration.KafkaProperties
import com.rarible.blockchain.scanner.publisher.LogRecordEventPublisher
import com.rarible.blockchain.scanner.solana.EnableSolanaScanner
import com.rarible.blockchain.scanner.solana.client.SolanaApi
import com.rarible.blockchain.scanner.solana.client.SolanaHttpRpcApi
import com.rarible.blockchain.scanner.solana.configuration.SolanaBlockchainScannerProperties
import com.rarible.core.application.ApplicationEnvironmentInfo
import com.rarible.core.entity.reducer.service.StreamFullReduceService
import com.rarible.protocol.solana.common.configuration.SolanaIndexerProperties
import com.rarible.protocol.solana.common.records.SubscriberGroup
import com.rarible.protocol.solana.nft.listener.block.cache.BlockCacheProperties
import com.rarible.protocol.solana.nft.listener.block.cache.BlockCacheRepository
import com.rarible.protocol.solana.nft.listener.block.cache.SolanaCachingApi
import com.rarible.protocol.solana.nft.listener.consumer.KafkaEntityEventConsumer
import com.rarible.protocol.solana.nft.listener.consumer.LogRecordEventListener
import com.rarible.protocol.solana.nft.listener.service.auction.house.AuctionHouseIdService
import com.rarible.protocol.solana.nft.listener.service.auction.house.AuctionHouseReducer
import com.rarible.protocol.solana.nft.listener.service.auction.house.AuctionHouseTemplateProvider
import com.rarible.protocol.solana.nft.listener.service.auction.house.AuctionHouseUpdateService
import com.rarible.protocol.solana.nft.listener.service.balance.BalanceIdService
import com.rarible.protocol.solana.nft.listener.service.balance.BalanceReducer
import com.rarible.protocol.solana.nft.listener.service.balance.BalanceTemplateProvider
import com.rarible.protocol.solana.nft.listener.service.balance.BalanceUpdateService
import com.rarible.protocol.solana.nft.listener.service.meta.MetaIdService
import com.rarible.protocol.solana.nft.listener.service.meta.MetaReducer
import com.rarible.protocol.solana.nft.listener.service.meta.MetaTemplateProvider
import com.rarible.protocol.solana.nft.listener.service.meta.MetaUpdateService
import com.rarible.protocol.solana.nft.listener.service.order.OrderIdService
import com.rarible.protocol.solana.nft.listener.service.order.OrderReducer
import com.rarible.protocol.solana.nft.listener.service.order.OrderTemplateProvider
import com.rarible.protocol.solana.nft.listener.service.order.OrderUpdateService
import com.rarible.protocol.solana.nft.listener.service.token.TokenIdService
import com.rarible.protocol.solana.nft.listener.service.token.TokenReducer
import com.rarible.protocol.solana.nft.listener.service.token.TokenTemplateProvider
import com.rarible.protocol.solana.nft.listener.service.token.TokenUpdateService
import com.rarible.protocol.solana.nft.listener.task.AuctionHouseStreamFullReduceService
import com.rarible.protocol.solana.nft.listener.task.BalanceStreamFullReduceService
import com.rarible.protocol.solana.nft.listener.task.MetaplexMetaStreamFullReduceService
import com.rarible.protocol.solana.nft.listener.task.TokenStreamFullReduceService
import com.rarible.solana.block.BlockCompressor
import com.rarible.solana.block.SolanaBlockCompressingApi
import io.micrometer.core.instrument.MeterRegistry
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableSolanaScanner
@EnableMongock
class BlockchainScannerConfiguration(
    private val solanaIndexerProperties: SolanaIndexerProperties,
    private val meterRegistry: MeterRegistry,
    private val applicationEnvironmentInfo: ApplicationEnvironmentInfo,
    private val blockCacheProperties: BlockCacheProperties
) {

    @Bean("solanaHttpRpcApi")
    fun solanaHttpRpcApi(properties: SolanaBlockchainScannerProperties): SolanaHttpRpcApi {
        logger.info("Solana HTTP RPC API settings: URLs = ${properties.rpcApiUrls}, timeout = ${properties.rpcApiTimeout}")
        return SolanaHttpRpcApi(
            urls = properties.rpcApiUrls,
            timeoutMillis = properties.rpcApiTimeout
        )
    }

    @Bean("solanaBlockCompressingApi")
    fun solanaBlockCompressingApi(
        @Qualifier("solanaHttpRpcApi") solanaHttpRpcApi: SolanaHttpRpcApi
    ): SolanaBlockCompressingApi {
        val solanaProgramIdsToKeep = solanaIndexerProperties.featureFlags.blockCompressorProgramIdsToKeep
            .takeIf { it.isNotEmpty() } ?: BlockCompressor.DEFAULT_COMPRESSOR_PROGRAM_IDS
        logger.info("Solana blocks will be compressed by keeping only the following programs: $solanaProgramIdsToKeep")
        return SolanaBlockCompressingApi(
            httpApi = solanaHttpRpcApi,
            blockCompressor = BlockCompressor(solanaProgramIdsToKeep)
        )
    }

    @Bean
    fun solanaApi(
        @Qualifier("solanaBlockCompressingApi") solanaBlockCompressingApi: SolanaBlockCompressingApi,
        repository: BlockCacheRepository,
        properties: SolanaBlockchainScannerProperties,
        meterRegistry: MeterRegistry
    ): SolanaApi {
        return if (solanaIndexerProperties.featureFlags.enableCacheApi) {
            logger.info("Solana caching client is enabled")
            SolanaCachingApi(
                repository = repository,
                enableBatchedGetBlocks = blockCacheProperties.enableBatch,
                meterRegistry = meterRegistry,
                delegate = solanaBlockCompressingApi
            )
        } else {
            solanaBlockCompressingApi
        }
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
        logger.info("Initializing KafkaEntityEventConsumer to connect to ${solanaIndexerProperties.kafkaReplicaSet}")
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

    @Bean
    fun metaplexMetaStreamReducer(
        metaUpdateService: MetaUpdateService,
        metaIdService: MetaIdService,
        metaTemplateProvider: MetaTemplateProvider,
        metaReducer: MetaReducer
    ) = MetaplexMetaStreamFullReduceService(
        metaUpdateService,
        metaIdService,
        metaTemplateProvider,
        metaReducer
    )

    @Bean
    fun auctionHouseStreamReducer(
        auctionHouseUpdateService: AuctionHouseUpdateService,
        auctionHouseIdService: AuctionHouseIdService,
        auctionHouseTemplateProvider: AuctionHouseTemplateProvider,
        auctionHouseReducer: AuctionHouseReducer
    ) = AuctionHouseStreamFullReduceService(
        auctionHouseUpdateService,
        auctionHouseIdService,
        auctionHouseTemplateProvider,
        auctionHouseReducer
    )

    private companion object {
        val logger: Logger = LoggerFactory.getLogger(BlockchainScannerConfiguration::class.java)
    }
}
