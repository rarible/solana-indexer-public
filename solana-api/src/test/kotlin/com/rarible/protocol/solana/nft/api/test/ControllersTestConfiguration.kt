package com.rarible.protocol.solana.nft.api.test

import com.rarible.protocol.solana.common.meta.TokenMetaService
import com.rarible.protocol.solana.nft.api.service.BalanceService
import com.rarible.protocol.solana.nft.api.service.TokenService
import io.mockk.mockk
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

@TestConfiguration
class ControllersTestConfiguration {
    @Bean
    @Primary
    fun testTokenService(): TokenService = mockk()

    @Bean
    @Primary
    fun testMetadataService(): TokenMetaService = mockk()

    @Bean
    @Primary
    fun testBalanceService(): BalanceService = mockk()
}
