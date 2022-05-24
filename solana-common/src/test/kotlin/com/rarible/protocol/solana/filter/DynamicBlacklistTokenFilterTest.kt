package com.rarible.protocol.solana.filter

import com.rarible.core.test.data.randomString
import com.rarible.protocol.solana.AbstractIntegrationTest
import com.rarible.protocol.solana.common.filter.token.dynamic.DynamicBlacklistSolanaTokenFilter
import com.rarible.protocol.solana.common.repository.DynamicBlacklistedTokenRepository
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class DynamicBlacklistTokenFilterTest : AbstractIntegrationTest() {

    @Autowired
    private lateinit var dynamicBlacklistedTokenRepository: DynamicBlacklistedTokenRepository

    @Test
    fun `black listed mint`() = runBlocking<Unit> {
        val mint = randomString()
        dynamicBlacklistedTokenRepository.save(mint, "test")
        val filter = DynamicBlacklistSolanaTokenFilter(
            dynamicBlacklistedTokenRepository = dynamicBlacklistedTokenRepository
        )
        assertThat(filter.isAcceptableToken(mint)).isFalse
        assertThat(filter.isAcceptableToken(randomString())).isTrue()
    }

    @Test
    fun `is acceptable`() = runBlocking<Unit> {
        val filter = DynamicBlacklistSolanaTokenFilter(
            dynamicBlacklistedTokenRepository = dynamicBlacklistedTokenRepository
        )

        val mint = randomString()
        assertThat(filter.isAcceptableToken(mint)).isTrue
        filter.addToBlacklist(mapOf(mint to "test"))
        assertThat(filter.isAcceptableToken(mint)).isFalse
    }

}