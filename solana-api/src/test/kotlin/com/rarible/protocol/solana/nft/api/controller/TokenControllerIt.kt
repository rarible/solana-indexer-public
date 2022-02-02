package com.rarible.protocol.solana.nft.api.controller

import com.rarible.protocol.solana.common.repository.TokenRepository
import com.rarible.protocol.solana.common.converter.TokenConverter
import com.rarible.protocol.solana.nft.api.data.createRandomToken
import com.rarible.protocol.solana.nft.api.test.AbstractIntegrationTest
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class TokenControllerIt : AbstractIntegrationTest() {

    @Autowired
    private lateinit var tokenRepository: TokenRepository

    @Test
    fun `find token by address`() = runBlocking<Unit> {
        val token = createRandomToken()
        tokenRepository.save(token)
        assertThat(tokenControllerApi.getTokenByAddress(token.mint).awaitFirst())
            .isEqualTo(TokenConverter.convert(token))
    }
}
