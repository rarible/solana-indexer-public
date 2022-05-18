package com.rarible.protocol.solana.filter

import com.rarible.protocol.solana.common.filter.token.SolanaWhiteListTokenFilter
import com.rarible.protocol.solana.common.filter.token.TokenListFileReader
import com.rarible.protocol.solana.test.randomMint
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SolanaWhiteListTokenFilterTest {

    private val whiteListFilter = SolanaWhiteListTokenFilter(
        TokenListFileReader("/whitelist").readTokens(listOf("degenape"))
    )

    @Test
    fun `white list`() = runBlocking<Unit> {
        val tokenFromWhiteList = "127RACV8SfCbbVrLdRbukh63zCDcubW4xVGh6aV6pnZi"
        val randomToken = randomMint()
        val emptyToken = ""

        assertThat(whiteListFilter.isAcceptableToken(tokenFromWhiteList)).isTrue
        assertThat(whiteListFilter.isAcceptableToken(randomToken)).isFalse
        assertThat(whiteListFilter.isAcceptableToken(emptyToken)).isFalse()
    }
}