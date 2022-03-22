package com.rarible.protocol.solana.nft.listener.service.subscribers.filter

import com.rarible.core.test.data.randomString
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SolanaWhiteListTokenFilterTest {

    val whiteListFilter = SolanaWhiteListTokenFilter(
        NftTokenReader("/whitelist").readTokens(listOf("degenape"))
    )

    @Test
    fun `white list`() {
        val tokenFromWhiteList = "127RACV8SfCbbVrLdRbukh63zCDcubW4xVGh6aV6pnZi"
        val randomToken = randomString()
        val emptyToken = ""

        assertThat(whiteListFilter.isAcceptableToken(tokenFromWhiteList)).isTrue()
        assertThat(whiteListFilter.isAcceptableToken(randomToken)).isFalse()
        assertThat(whiteListFilter.isAcceptableToken(emptyToken)).isFalse()
    }
}