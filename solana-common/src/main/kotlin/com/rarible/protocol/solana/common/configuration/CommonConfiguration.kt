package com.rarible.protocol.solana.common.configuration

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(
    RepositoryConfiguration::class,
    EventProducerConfiguration::class
)
@EnableConfigurationProperties(SolanaIndexerProperties::class)
class CommonConfiguration
