package com.rarible.protocol.solana.nft.api.controller

import com.rarible.core.test.data.randomString
import com.rarible.protocol.solana.common.converter.TokenWithMetaConverter
import com.rarible.protocol.solana.common.converter.TokenMetaConverter
import com.rarible.protocol.solana.nft.api.test.AbstractControllerTest
import com.rarible.protocol.solana.test.createRandomToken
import com.rarible.protocol.solana.test.createRandomTokenMeta
import com.rarible.protocol.solana.test.createRandomTokenWithMeta
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
        val tokenWithMeta = createRandomTokenWithMeta()
        coEvery { testTokenApiService.getTokenWithMeta(tokenWithMeta.token.mint) } returns tokenWithMeta
        assertThat(tokenControllerApi.getTokenByAddress(tokenWithMeta.token.mint).awaitFirst())
            .isEqualTo(TokenWithMetaConverter.convert(tokenWithMeta))
    }

    @Test
    fun `find tokens by collection`() = runBlocking<Unit> {
        val tokenWithMeta = createRandomTokenWithMeta()
        val collectionAddress = randomString()
        coEvery { testTokenApiService.getTokensWithMetaByCollection(collectionAddress) } returns listOf(tokenWithMeta)
        assertThat(tokenControllerApi.getTokensByCollection(collectionAddress).awaitFirst())
            .isEqualTo(TokenWithMetaConverter.convert(listOf(tokenWithMeta)))
    }

    @Test
    fun `load meta by address`() = runBlocking<Unit> {
        val token = createRandomToken()
        val tokenMeta = createRandomTokenMeta()
        coEvery {
            @Suppress("BlockingMethodInNonBlockingContext")
            testTokenMetaService.loadTokenMeta(token.id)
        } returns tokenMeta
        assertThat(tokenControllerApi.getTokenMetaByAddress(token.mint).awaitFirst())
            .isEqualTo(TokenMetaConverter.convert(tokenMeta))
    }

}
