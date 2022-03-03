package com.rarible.protocol.solana.nft.api.service

import com.rarible.protocol.solana.common.model.TokenWithMeta
import com.rarible.protocol.solana.common.repository.TokenRepository
import com.rarible.protocol.solana.test.createRandomMetaplexMetaFieldsCollection
import com.rarible.protocol.solana.test.createRandomMetaplexOffChainMetaFields
import com.rarible.protocol.solana.test.createRandomToken
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class TokenApiServiceIt : AbstractMetaAwareIntegrationTest() {

    @Autowired
    private lateinit var tokenRepository: TokenRepository

    @Autowired
    private lateinit var tokenApiService: TokenApiService

    @Test
    fun `find token with meta by token address`() = runBlocking<Unit> {
        val token = createRandomToken()
        tokenRepository.save(token)
        val tokenMeta = saveRandomMetaplexOnChainAndOffChainMeta(token.mint)
        assertThat(tokenApiService.getTokenWithMeta(token.mint))
            .isEqualTo(TokenWithMeta(token, tokenMeta))
    }

    @Test
    fun `find tokens with meta by metaplex on-chain collection address`() = runBlocking<Unit> {
        val token = createRandomToken()
        val token2 = createRandomToken()
        val token3 = createRandomToken()
        tokenRepository.save(token)
        tokenRepository.save(token2)
        tokenRepository.save(token3)

        val collection = createRandomMetaplexMetaFieldsCollection()
        val tokenMeta = saveRandomMetaplexOnChainAndOffChainMeta(
            tokenAddress = token.mint,
            metaplexMetaCustomizer = { this.copy(metaFields = this.metaFields.copy(collection = collection)) }
        )
        val tokenMeta2 = saveRandomMetaplexOnChainAndOffChainMeta(
            tokenAddress = token2.mint,
            metaplexMetaCustomizer = { this.copy(metaFields = this.metaFields.copy(collection = collection)) }
        )
        assertThat(tokenApiService.getTokensWithMetaByCollection(collection.address).toList())
            .isEqualTo(
                listOf(
                    TokenWithMeta(token, tokenMeta),
                    TokenWithMeta(token2, tokenMeta2)
                ).sortedBy { it.token.mint })
    }

    @Test
    fun `find tokens by off-chain collection hash`() = runBlocking<Unit> {
        val token = createRandomToken()
        val token2 = createRandomToken()
        val token3 = createRandomToken()
        tokenRepository.save(token)
        tokenRepository.save(token2)
        tokenRepository.save(token3)

        val collection = createRandomMetaplexOffChainMetaFields().collection!!
        val tokenMeta = saveRandomMetaplexOnChainAndOffChainMeta(
            tokenAddress = token.mint,
            metaplexOffChainMetaCustomizer = { this.copy(metaFields = this.metaFields.copy(collection = collection)) }
        )
        val tokenMeta2 = saveRandomMetaplexOnChainAndOffChainMeta(
            tokenAddress = token2.mint,
            metaplexOffChainMetaCustomizer = { this.copy(metaFields = this.metaFields.copy(collection = collection)) }
        )
        assertThat(tokenApiService.getTokensWithMetaByCollection(collection.hash).toList())
            .isEqualTo(
                listOf(
                    TokenWithMeta(token, tokenMeta),
                    TokenWithMeta(token2, tokenMeta2)
                ).sortedBy { it.token.mint })
    }

}
