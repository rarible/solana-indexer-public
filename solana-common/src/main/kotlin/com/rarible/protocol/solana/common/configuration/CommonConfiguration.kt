package com.rarible.protocol.solana.common.configuration

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(RepositoryConfiguration::class)
class CommonConfiguration
