package com.rarible.protocol.solana.nft.api.service

import com.rarible.protocol.solana.common.repository.MetaplexMetaRepository
import com.rarible.protocol.solana.common.repository.MetaplexOffChainMetaRepository
import com.rarible.protocol.solana.common.repository.TokenRepository
import com.rarible.protocol.solana.nft.api.test.AbstractIntegrationTest
import com.rarible.protocol.solana.test.createRandomMetaplexMeta
import com.rarible.protocol.solana.test.createRandomMetaplexMetaFieldsCollection
import com.rarible.protocol.solana.test.createRandomMetaplexOffChainMeta
import com.rarible.protocol.solana.test.createRandomToken
import com.rarible.protocol.solana.test.createRandomTokenWithMeta
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class TokenApiServiceIt : AbstractIntegrationTest() {

    @Autowired
    private lateinit var tokenRepository: TokenRepository

    @Autowired
    private lateinit var metaplexMetaRepository: MetaplexMetaRepository

    @Autowired
    private lateinit var metaplexOffChainMetaRepository: MetaplexOffChainMetaRepository

    @Autowired
    private lateinit var tokenApiService: TokenApiService

    @Test
    fun `find tokens with meta by metaplex collection`() = runBlocking<Unit> {
        val token = createRandomTokenWithMeta()
        val token2 = createRandomTokenWithMeta()
        val token3 = createRandomTokenWithMeta()
        tokenRepository.save(token.token)
        tokenRepository.save(token2.token)
        tokenRepository.save(token3.token)
        val collection = createRandomMetaplexMetaFieldsCollection()
        val metaplexMeta = createRandomMetaplexMeta().let {
            it.copy(
                tokenAddress = token.token.mint,
                metaFields = it.metaFields.copy(
                    collection = collection
                )
            )
        }
        val metaplexMeta2 = createRandomMetaplexMeta().let {
            it.copy(
                tokenAddress = token2.token.mint,
                metaFields = it.metaFields.copy(
                    collection = collection
                )
            )
        }
        metaplexMetaRepository.save(metaplexMeta)
        metaplexMetaRepository.save(metaplexMeta2)
        assertThat(tokenApiService.getTokensWithMetaByCollection(collection.address).toList())
            .isEqualTo(listOf(token, token2).sortedBy { it.token.mint })
    }

    @Test
    fun `find tokens by off-chain collection hash`() = runBlocking<Unit> {
        val token = createRandomToken()
        val token2 = createRandomToken()
        val token3 = createRandomToken()
        tokenRepository.save(token)
        tokenRepository.save(token2)
        tokenRepository.save(token3)

        val offChainMeta = createRandomMetaplexOffChainMeta().copy(tokenAddress = token.mint)
        val collection = offChainMeta.metaFields.collection!!
        val offChainMeta2 = createRandomMetaplexOffChainMeta().copy(
            tokenAddress = token2.mint,
            metaFields = offChainMeta.metaFields.copy(
                collection = collection
            )
        )
        val offChainMeta3 = createRandomMetaplexOffChainMeta().copy(tokenAddress = token3.mint)
        metaplexOffChainMetaRepository.save(offChainMeta)
        metaplexOffChainMetaRepository.save(offChainMeta2)
        metaplexOffChainMetaRepository.save(offChainMeta3)

        assertThat(tokenApiService.getTokensWithMetaByCollection(collection.hash).toList())
            .isEqualTo(listOf(token, token2).sortedBy { it.mint })
    }

}
