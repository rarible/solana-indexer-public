package com.rarible.protocol.solana.nft.api.test

import com.rarible.protocol.solana.common.meta.MetaplexOffChainMetaLoader
import io.mockk.mockk
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

@TestConfiguration
class ControllersTestConfiguration {

    @Bean
    @Primary
    fun testMetaplexOffChainMetaLoader(): MetaplexOffChainMetaLoader = mockk()

}
