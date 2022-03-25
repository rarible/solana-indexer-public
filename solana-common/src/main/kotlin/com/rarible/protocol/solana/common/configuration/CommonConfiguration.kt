package com.rarible.protocol.solana.common.configuration

import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import java.time.Clock

@Configuration
@Import(
    RepositoryConfiguration::class,
    EventProducerConfiguration::class,
    SolanaMetaConfiguration::class,
    SolanaServiceConfiguration::class
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
}
