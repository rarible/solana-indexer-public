package com.rarible.protocol.solana.common.configuration

import com.rarible.blockchain.scanner.framework.data.LogRecordEvent
import com.rarible.blockchain.scanner.publisher.LogRecordEventPublisher
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import java.time.Clock

@Configuration
@Import(
    RepositoryConfiguration::class,
    EventProducerConfiguration::class,
    SolanaMetaConfiguration::class
)
@EnableConfigurationProperties(SolanaIndexerProperties::class)
class CommonConfiguration(
    private val properties: SolanaIndexerProperties
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Bean
    fun solanaClock(): Clock = Clock.systemUTC()

    @Bean
    fun featureFlags(): FeatureFlags {
        logger.info("Activated feature flags: {}", properties.featureFlags)
        return properties.featureFlags
    }

    @Bean
    @ConditionalOnProperty(
        value = ["common.featureFlags.disableKafkaTopicsForRecords"],
        havingValue = "true",
        matchIfMissing = true
    )
    fun producer(): LogRecordEventPublisher {
        logger.info("Activated feature: disable Kafka topics for log recods")
        return object : LogRecordEventPublisher {
            override suspend fun publish(groupId: String, logRecordEvents: List<LogRecordEvent<*>>) = Unit
        }
    }
}
