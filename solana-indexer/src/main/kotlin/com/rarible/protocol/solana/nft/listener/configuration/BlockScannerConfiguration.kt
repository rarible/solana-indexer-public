package com.rarible.protocol.solana.nft.listener.configuration

import com.rarible.blockchain.scanner.configuration.KafkaProperties
import com.rarible.blockchain.scanner.solana.EnableSolanaScanner
import com.rarible.blockchain.scanner.solana.configuration.SolanaBlockchainScannerProperties
import com.rarible.core.application.ApplicationEnvironmentInfo
import com.rarible.protocol.solana.common.configuration.SolanaIndexerProperties
import com.rarible.protocol.solana.nft.listener.consumer.EntityEventListener
import com.rarible.protocol.solana.nft.listener.consumer.KafkaEntityEventConsumer
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableSolanaScanner
class BlockchainScannerConfiguration(
    private val solanaIndexerProperties: SolanaIndexerProperties,
    private val meterRegistry: MeterRegistry,
    private val applicationEnvironmentInfo: ApplicationEnvironmentInfo
) {
    @Bean
    fun entityEventConsumer(
        entityEventListener: List<EntityEventListener>,
        solanaBlockchainScannerProperties: SolanaBlockchainScannerProperties
    ): KafkaEntityEventConsumer {
        return KafkaEntityEventConsumer(
            properties = KafkaProperties(
                brokerReplicaSet = solanaIndexerProperties.kafkaReplicaSet,
            ),
            meterRegistry = meterRegistry,
            applicationEnvironmentInfo = applicationEnvironmentInfo,
            solanaBlockchainScannerProperties = solanaBlockchainScannerProperties
        ).apply { start(entityEventListener) }
    }
}
