package com.rarible.protocol.solana.nft.api.controller

import com.rarible.core.test.data.randomString
import com.rarible.protocol.solana.common.converter.TokenConverter
import com.rarible.protocol.solana.common.converter.TokenMetaConverter
import com.rarible.protocol.solana.nft.api.test.AbstractControllerTest
import com.rarible.protocol.solana.test.createRandomToken
import com.rarible.protocol.solana.test.createRandomTokenMeta
import io.mockk.coEvery
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TokenControllerIt : AbstractControllerTest() {

    @Test
    fun `find token by address`() = runBlocking<Unit> {
        val token = createRandomToken()
        coEvery { testTokenService.getToken(token.mint) } returns token
        assertThat(tokenControllerApi.getTokenByAddress(token.mint).awaitFirst())
            .isEqualTo(TokenConverter.convert(token))
    }

    @Test
    fun `find tokens by collection - on-chain collection`() = runBlocking<Unit> {
        val token = createRandomToken()
        val collectionAddress = randomString()
        coEvery { testTokenService.getTokensByMetaplexCollectionAddress(collectionAddress) } returns flowOf(token)
        coEvery { testTokenService.getTokensByOffChainCollectionHash(collectionAddress) } returns emptyFlow()
        assertThat(tokenControllerApi.getTokensByCollection(collectionAddress).awaitFirst())
            .isEqualTo(TokenConverter.convert(listOf(token)))
    }

    @Test
    fun `find tokens by collection - off-chain collection`() = runBlocking<Unit> {
        val token = createRandomToken()
        val collectionAddress = randomString()
        coEvery { testTokenService.getTokensByOffChainCollectionHash(collectionAddress) } returns flowOf(token)
        coEvery { testTokenService.getTokensByMetaplexCollectionAddress(collectionAddress) } returns emptyFlow()
        assertThat(tokenControllerApi.getTokensByCollection(collectionAddress).awaitFirst())
            .isEqualTo(TokenConverter.convert(listOf(token)))
    }

    @Test
    fun `load meta by address`() = runBlocking<Unit> {
        val token = createRandomToken()
        val tokenMeta = createRandomTokenMeta()
        coEvery {
            @Suppress("BlockingMethodInNonBlockingContext")
            testTokenMetaService.getTokenMeta(token.id)
        } returns tokenMeta
        assertThat(tokenControllerApi.getTokenMetaByAddress(token.mint).awaitFirst())
            .isEqualTo(TokenMetaConverter.convert(tokenMeta))
    }

}
