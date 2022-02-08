package com.rarible.protocol.solana.nft.api.controller

import com.rarible.protocol.solana.common.converter.TokenConverter
import com.rarible.protocol.solana.common.converter.TokenMetaConverter
import com.rarible.protocol.solana.common.meta.TokenMetadataService
import com.rarible.protocol.solana.common.repository.TokenRepository
import com.rarible.protocol.solana.nft.api.test.AbstractIntegrationTest
import com.rarible.protocol.solana.test.createRandomToken
import com.rarible.protocol.solana.test.createRandomTokenMetadata
import io.mockk.coEvery
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class TokenControllerIt : AbstractIntegrationTest() {

    @Autowired
    private lateinit var tokenRepository: TokenRepository

    @Autowired
    private lateinit var testTokenMetadataService: TokenMetadataService

    @Test
    fun `find token by address`() = runBlocking<Unit> {
        val token = createRandomToken()
        tokenRepository.save(token)
        assertThat(tokenControllerApi.getTokenByAddress(token.mint).awaitFirst())
            .isEqualTo(TokenConverter.convert(token))
    }

    @Test
    fun `load meta by address`() = runBlocking<Unit> {
        val token = createRandomToken()
        val tokenMetadata = createRandomTokenMetadata()
        coEvery {
            @Suppress("BlockingMethodInNonBlockingContext")
            testTokenMetadataService.getTokenMetadata(token.id)
        } returns tokenMetadata
        assertThat(tokenControllerApi.getTokenMetaByAddress(token.mint).awaitFirst())
            .isEqualTo(TokenMetaConverter.convert(tokenMetadata))
    }

}
