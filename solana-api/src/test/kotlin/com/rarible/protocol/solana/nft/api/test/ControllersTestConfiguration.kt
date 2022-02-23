package com.rarible.protocol.solana.nft.api.test

import com.rarible.protocol.solana.common.meta.TokenMetaService
import com.rarible.protocol.solana.nft.api.service.BalanceApiService
import com.rarible.protocol.solana.nft.api.service.TokenApiService
import io.mockk.mockk
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

@TestConfiguration
class ControllersTestConfiguration {
    @Bean
    @Primary
    fun testTokenService(): TokenApiService = mockk()

    @Bean
    @Primary
    fun testMetadataService(): TokenMetaService = mockk()

    @Bean
    @Primary
    fun testBalanceService(): BalanceApiService = mockk()
}
