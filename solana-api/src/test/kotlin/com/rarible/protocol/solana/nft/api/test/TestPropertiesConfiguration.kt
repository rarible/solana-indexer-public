package com.rarible.protocol.solana.nft.api.test

import com.rarible.protocol.solana.common.meta.TokenMetadataService
import io.mockk.mockk
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

@TestConfiguration
class TestPropertiesConfiguration {
    @Bean
    @Primary
    fun testMetadataService(): TokenMetadataService = mockk()
}
