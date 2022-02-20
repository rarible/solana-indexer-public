package com.rarible.protocol.solana.nft.api.service

import com.rarible.core.test.data.randomString
import com.rarible.protocol.solana.common.meta.MetaplexOffChainCollectionHash
import com.rarible.protocol.solana.common.model.TokenOffChainCollection
import com.rarible.protocol.solana.common.repository.MetaplexMetaRepository
import com.rarible.protocol.solana.common.repository.TokenOffChainCollectionRepository
import com.rarible.protocol.solana.common.repository.TokenRepository
import com.rarible.protocol.solana.nft.api.test.AbstractIntegrationTest
import com.rarible.protocol.solana.test.createRandomMetaplexMeta
import com.rarible.protocol.solana.test.createRandomMetaplexMetaFieldsCollection
import com.rarible.protocol.solana.test.createRandomToken
import com.rarible.protocol.solana.test.randomUrl
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class TokenServiceIt : AbstractIntegrationTest() {

    @Autowired
    private lateinit var tokenRepository: TokenRepository

    @Autowired
    private lateinit var tokenOffChainCollectionRepository: TokenOffChainCollectionRepository

    @Autowired
    private lateinit var metaplexMetaRepository: MetaplexMetaRepository

    @Autowired
    private lateinit var tokenService: TokenService

    @Test
    fun `find tokens by metaplex collection`() = runBlocking<Unit> {
        val token = createRandomToken()
        val token2 = createRandomToken()
        val token3 = createRandomToken()
        tokenRepository.save(token)
        tokenRepository.save(token2)
        tokenRepository.save(token3)
        val collection = createRandomMetaplexMetaFieldsCollection()
        val metaplexMeta = createRandomMetaplexMeta().let {
            it.copy(
                tokenAddress = token.mint,
                metaFields = it.metaFields.copy(
                    collection = collection
                )
            )
        }
        val metaplexMeta2 = createRandomMetaplexMeta().let {
            it.copy(
                tokenAddress = token2.mint,
                metaFields = it.metaFields.copy(
                    collection = collection
                )
            )
        }
        metaplexMetaRepository.save(metaplexMeta)
        metaplexMetaRepository.save(metaplexMeta2)
        assertThat(tokenService.getTokensByMetaplexCollectionAddress(collection.address).toList())
            .isEqualTo(listOf(token, token2).sortedBy { it.mint })
    }

    @Test
    fun `find tokens by off-chain collection hash`() = runBlocking<Unit> {
        val token = createRandomToken()
        val token2 = createRandomToken()
        val token3 = createRandomToken()
        tokenRepository.save(token)
        tokenRepository.save(token2)
        tokenRepository.save(token3)

        val name = randomString()
        val family = randomString()
        val tokenOffChainCollection = TokenOffChainCollection(
            hash = MetaplexOffChainCollectionHash.calculateCollectionHash(
                name = name,
                family = family,
                creators = listOf(randomString())
            ),
            name = name,
            family = family,
            metadataUrl = randomUrl(),
            tokenAddress = token.mint
        )
        tokenOffChainCollectionRepository.save(tokenOffChainCollection)
        tokenOffChainCollectionRepository.save(
            tokenOffChainCollection.copy(
                id = ObjectId().toHexString(),
                tokenAddress = token2.mint
            )
        )

        assertThat(tokenService.getTokensByOffChainCollectionHash(tokenOffChainCollection.hash).toList())
            .isEqualTo(listOf(token, token2).sortedBy { it.mint })
    }

}
