package com.rarible.protocol.solana.nft.api.controller

import com.rarible.core.common.nowMillis
import com.rarible.protocol.solana.common.converter.TokenMetaConverter
import com.rarible.protocol.solana.common.converter.TokenWithMetaConverter
import com.rarible.protocol.solana.common.meta.TokenMetaParser
import com.rarible.protocol.solana.common.model.MetaplexMetaFields
import com.rarible.protocol.solana.common.model.MetaplexOffChainMetaFields
import com.rarible.protocol.solana.common.model.MetaplexTokenCreator
import com.rarible.protocol.solana.common.model.TokenWithMeta
import com.rarible.protocol.solana.common.repository.TokenRepository
import com.rarible.protocol.solana.dto.RoyaltyDto
import com.rarible.protocol.solana.dto.TokensDto
import com.rarible.protocol.solana.nft.api.test.AbstractControllerTest
import com.rarible.protocol.solana.test.createRandomMetaplexMeta
import com.rarible.protocol.solana.test.createRandomMetaplexMetaFieldsCollection
import com.rarible.protocol.solana.test.createRandomMetaplexOffChainMeta
import com.rarible.protocol.solana.test.createRandomMetaplexOffChainMetaFields
import com.rarible.protocol.solana.test.createRandomToken
import com.rarible.protocol.solana.test.randomMint
import io.mockk.coEvery
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Instant

class TokenControllerFt : AbstractControllerTest() {

    @Autowired
    private lateinit var tokenRepository: TokenRepository

    @Test
    fun `find token by address`() = runBlocking<Unit> {
        val tokenWithMeta = saveTokenWithMeta()

        val result = tokenControllerApi.getTokenByAddress(tokenWithMeta.token.mint).awaitFirst()
        val expected = TokenWithMetaConverter.convert(tokenWithMeta)

        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `find all tokens - without filter`() = runBlocking<Unit> {
        val now = nowMillis()

        val collection = createRandomMetaplexOffChainMetaFields().collection!!
        val tokenWithMeta1 = saveTokenWithMeta(offCollection = collection, updatedAt = now)
        val tokenWithMeta2 = saveTokenWithMeta(offCollection = collection, updatedAt = now.plusSeconds(1))

        // Should be sorted from newest to oldest

        //Page 1
        val expected1 = TokensDto(
            tokens = listOf(tokenWithMeta2).map { TokenWithMetaConverter.convert(it) },
            continuation = "${tokenWithMeta2.token.updatedAt.toEpochMilli()}_${tokenWithMeta2.token.mint}"
        )
        val page1 = getAllTokens(continuation = null, size = 1)
        assertThat(page1).isEqualTo(expected1)

        // Page 2
        val expected2 = TokensDto(
            tokens = listOf(tokenWithMeta1).map { TokenWithMetaConverter.convert(it) },
            continuation = "${tokenWithMeta1.token.updatedAt.toEpochMilli()}_${tokenWithMeta1.token.mint}"
        )
        val page2 = getAllTokens(continuation = page1.continuation, size = 1)
        assertThat(page2).isEqualTo(expected2)

        // Empty page
        val expected3 = TokensDto()
        val page3 = getAllTokens(continuation = page2.continuation, size = 1)
        assertThat(page3).isEqualTo(expected3)
    }

    @Test
    fun `find all tokens - with time filter`() = runBlocking<Unit> {
        val now = nowMillis()

        val collection = createRandomMetaplexOffChainMetaFields().collection!!
        val tokenWithMeta1 = saveTokenWithMeta(offCollection = collection, updatedAt = now)
        val tokenWithMeta2 = saveTokenWithMeta(offCollection = collection, updatedAt = now.minusSeconds(10))
        val tokenWithMeta3 = saveTokenWithMeta(offCollection = collection, updatedAt = now.minusSeconds(20))
        val tokenWithMeta4 = saveTokenWithMeta(offCollection = collection, updatedAt = now.minusSeconds(30))

        val from = tokenWithMeta4.token.updatedAt
        val to = tokenWithMeta1.token.updatedAt

        // Should be sorted from newest to oldest

        //Page 1
        val expected1 = TokensDto(
            tokens = listOf(tokenWithMeta2).map { TokenWithMetaConverter.convert(it) },
            continuation = "${tokenWithMeta2.token.updatedAt.toEpochMilli()}_${tokenWithMeta2.token.mint}"
        )
        val page1 = getAllTokens(from, to, null, 1)
        assertThat(page1).isEqualTo(expected1)

        // Page 2
        val expected2 = TokensDto(
            tokens = listOf(tokenWithMeta3).map { TokenWithMetaConverter.convert(it) },
            continuation = null
        )
        val page2 = getAllTokens(from, to, page1.continuation, 2)
        assertThat(page2).isEqualTo(expected2)
    }

    @Test
    fun `get token royalties`() = runBlocking<Unit> {
        val token = tokenRepository.save(createRandomToken())
        saveRandomMetaplexOnChainAndOffChainMeta(
            tokenAddress = token.mint,
            metaplexMetaCustomizer = {
                this.copy(
                    metaFields = this.metaFields.copy(
                        sellerFeeBasisPoints = 1000,
                        creators = listOf(
                            MetaplexTokenCreator("a", 40),
                            MetaplexTokenCreator("b", 60)
                        )
                    )
                )
            }
        )

        val result = tokenControllerApi.getTokenRoyaltiesByAddress(token.mint).awaitFirst().royalties

        val expected = listOf(
            RoyaltyDto("a", 400),
            RoyaltyDto("b", 600)
        )

        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `find tokens by collection V1`() = runBlocking<Unit> {

        val collection = createRandomMetaplexOffChainMetaFields().collection!!
        val tokenWithMeta1 = saveTokenWithMeta(mint = "b", offCollection = collection)
        val tokenWithMeta2 = saveTokenWithMeta(mint = "a", offCollection = collection)

        // Should not be found
        saveTokenWithMeta(onCollection = createRandomMetaplexMetaFieldsCollection())

        // Should be sorted a -> b
        val expected = TokensDto(
            tokens = listOf(tokenWithMeta2, tokenWithMeta1).map { TokenWithMetaConverter.convert(it) },
            continuation = null
        )

        val result = tokenControllerApi.getTokensByCollection(collection.hash, null, 50).awaitFirst()

        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `find tokens by collection V2`() = runBlocking<Unit> {

        val collection = createRandomMetaplexMetaFieldsCollection()
        val tokenWithMeta1 = saveTokenWithMeta(mint = "b", onCollection = collection)
        val tokenWithMeta2 = saveTokenWithMeta(mint = "a", onCollection = collection)

        // Should not be found
        saveTokenWithMeta(onCollection = createRandomMetaplexMetaFieldsCollection())

        // Should be sorted a -> b
        val expected = TokensDto(
            tokens = listOf(tokenWithMeta2, tokenWithMeta1).map { TokenWithMetaConverter.convert(it) },
            continuation = null
        )

        val result = tokenControllerApi.getTokensByCollection(collection.address, null, 50).awaitFirst()

        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `load meta by address`() = runBlocking<Unit> {
        val token = tokenRepository.save(createRandomToken())
        val tokenMeta = metaplexMetaRepository.save(createRandomMetaplexMeta(token.mint))
        val offChainMeta = createRandomMetaplexOffChainMeta()

        val expected = TokenMetaParser.mergeOnChainAndOffChainMeta(tokenMeta.metaFields, offChainMeta.metaFields)

        coEvery {
            testMetaplexOffChainMetaLoader.loadMetaplexOffChainMeta(
                tokenAddress = eq(token.id),
                metaplexMetaFields = tokenMeta.metaFields
            )
        } returns offChainMeta

        assertThat(tokenControllerApi.getTokenMetaByAddress(token.mint).awaitFirst())
            .isEqualTo(TokenMetaConverter.convert(expected))
    }

    private suspend fun saveTokenWithMeta(
        mint: String = randomMint(),
        updatedAt: Instant = nowMillis(),
        onCollection: MetaplexMetaFields.Collection? = null,
        offCollection: MetaplexOffChainMetaFields.Collection? = null
    ): TokenWithMeta {
        val token = tokenRepository.save(createRandomToken(mint).copy(updatedAt = updatedAt))
        val tokenMeta = saveRandomMetaplexOnChainAndOffChainMeta(
            tokenAddress = token.mint,
            metaplexMetaCustomizer = {
                onCollection?.let { this.copy(metaFields = this.metaFields.copy(collection = onCollection)) } ?: this
            },
            metaplexOffChainMetaCustomizer = {
                offCollection?.let { this.copy(metaFields = this.metaFields.copy(collection = offCollection)) } ?: this
            }
        )
        return TokenWithMeta(token, tokenMeta)
    }

    private suspend fun getAllTokens(
        lastUpdatedFrom: Instant? = null,
        lastUpdatedTo: Instant? = null,
        continuation: String? = null,
        size: Int? = 50
    ): TokensDto {
        return tokenControllerApi.getAllTokens(
            false,
            lastUpdatedFrom?.toEpochMilli(),
            lastUpdatedTo?.toEpochMilli(),
            continuation,
            size
        ).awaitFirst()
    }

}
