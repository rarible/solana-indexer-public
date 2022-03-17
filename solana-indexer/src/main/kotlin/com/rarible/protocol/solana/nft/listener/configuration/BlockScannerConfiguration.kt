package com.rarible.protocol.solana.nft.listener.configuration

import com.github.cloudyrock.spring.v5.EnableMongock
import com.rarible.blockchain.scanner.configuration.KafkaProperties
import com.rarible.blockchain.scanner.publisher.LogRecordEventPublisher
import com.rarible.blockchain.scanner.solana.EnableSolanaScanner
import com.rarible.blockchain.scanner.solana.configuration.SolanaBlockchainScannerProperties
import com.rarible.core.application.ApplicationEnvironmentInfo
import com.rarible.core.lockredis.EnableRaribleRedisLock
import com.rarible.protocol.solana.common.configuration.SolanaIndexerProperties
import com.rarible.protocol.solana.nft.listener.consumer.KafkaEntityEventConsumer
import com.rarible.protocol.solana.nft.listener.consumer.LogRecordEventListener
import com.rarible.protocol.solana.nft.listener.service.subscribers.SubscriberGroup
import io.micrometer.core.instrument.MeterRegistry
import kotlinx.coroutines.runBlocking
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableSolanaScanner
@EnableRaribleRedisLock
@EnableMongock
class BlockchainScannerConfiguration(
    private val solanaIndexerProperties: SolanaIndexerProperties,
    private val meterRegistry: MeterRegistry,
    private val applicationEnvironmentInfo: ApplicationEnvironmentInfo
) {
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
}
