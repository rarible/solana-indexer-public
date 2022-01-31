package com.rarible.protocol.solana.nft.api.configuration

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(NftIndexerApiProperties::class)
class NftIndexerApiConfiguration(
    private val nftIndexerApiProperties: NftIndexerApiProperties
)
