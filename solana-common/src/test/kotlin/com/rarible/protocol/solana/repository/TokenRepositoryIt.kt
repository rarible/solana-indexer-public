package com.rarible.protocol.solana.repository

import com.rarible.protocol.solana.AbstractIntegrationTest
import com.rarible.protocol.solana.common.repository.TokenRepository
import com.rarible.protocol.solana.common.util.toBigInteger
import com.rarible.protocol.solana.test.createRandomToken
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
        tokenRepository.save(token)
        assertThat(tokenRepository.findByMint(token.mint)).isEqualTo(token)
    }

    @Test
    fun `save with max ULong supply and find by mint`() = runBlocking<Unit> {
        val token = createRandomToken().copy(supply = ULong.MAX_VALUE.toBigInteger())
        tokenRepository.save(token)
        assertThat(tokenRepository.findByMint(token.mint)).isEqualTo(token)
    }

}
