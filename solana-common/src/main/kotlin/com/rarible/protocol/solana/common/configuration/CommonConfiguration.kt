package com.rarible.protocol.solana.common.configuration

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

    @Bean
    fun solanaClock(): Clock = Clock.systemUTC()

    @Bean
    fun featureFlags(): FeatureFlags = properties.featureFlags
}
