package com.rarible.protocol.solana.nft.listener.configuration

import com.rarible.blockchain.scanner.configuration.KafkaProperties
import com.rarible.blockchain.scanner.solana.EnableSolanaScanner
import com.rarible.core.application.ApplicationEnvironmentInfo
import com.rarible.core.application.ApplicationInfo
import com.rarible.protocol.solana.nft.listener.consumer.EntityEventListener
import com.rarible.protocol.solana.nft.listener.consumer.KafkaEntityEventConsumer
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableSolanaScanner
@EnableConfigurationProperties(NftIndexerProperties::class)
class BlockchainScannerConfiguration(
    private val nftIndexerProperties: NftIndexerProperties,
    private val meterRegistry: MeterRegistry,
    private val applicationEnvironmentInfo: ApplicationEnvironmentInfo,
    private val applicationInfo: ApplicationInfo
) {
    @Bean
    fun entityEventConsumer(
        entityEventListener: List<EntityEventListener>
    ): KafkaEntityEventConsumer {
        return KafkaEntityEventConsumer(
            properties = KafkaProperties(
                brokerReplicaSet = nftIndexerProperties.kafkaReplicaSet,
            ),
            meterRegistry = meterRegistry,
            host = applicationEnvironmentInfo.host,
            environment = applicationEnvironmentInfo.name,
            service = applicationInfo.serviceName
        ).apply { start(entityEventListener) }
    }
}