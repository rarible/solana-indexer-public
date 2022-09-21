package com.rarible.protocol.solana.repository

import com.rarible.protocol.solana.AbstractIntegrationTest
import com.rarible.protocol.solana.common.repository.TokenRepository
import com.rarible.protocol.solana.common.util.toBigInteger
import com.rarible.protocol.solana.test.createRandomToken
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class TokenRepositoryIt : AbstractIntegrationTest() {

    @Autowired
    private lateinit var tokenRepository: TokenRepository

    @Test
    fun `save and find by mint`() = runBlocking<Unit> {
        val token = createRandomToken()
        val expected = tokenRepository.save(token)

        assertThat(tokenRepository.findByMint(token.mint)).isEqualTo(expected)
    }

    @Test
    fun `save and find by mints`() = runBlocking<Unit> {
        val tokens = (1..3).map { createRandomToken() }.sortedBy { it.id }
        val expected = tokens.asReversed().map { tokenRepository.save(it) }.sortedBy { it.id }

        assertThat(tokenRepository.findByMints(tokens.map { it.id }).toList()).isEqualTo(expected)
    }

    @Test
    fun `save with max ULong supply and find by mint`() = runBlocking<Unit> {
        val token = createRandomToken().copy(supply = ULong.MAX_VALUE.toBigInteger())
        val expected = tokenRepository.save(token)

        assertThat(tokenRepository.findByMint(token.mint)).isEqualTo(expected)
    }

}
