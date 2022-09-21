package com.rarible.protocol.solana.nft.api.configuration

import com.rarible.core.telemetry.actuator.WebRequestClientTagContributor
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(NftIndexerApiProperties::class)
class NftIndexerApiConfiguration(
    private val nftIndexerApiProperties: NftIndexerApiProperties
) {

    @Bean
    fun webRequestClientTagContributor(): WebRequestClientTagContributor {
        return WebRequestClientTagContributor()
    }

}
