package com.rarible.protocol.solana.nft.api.controller

import com.rarible.core.test.data.randomString
import com.rarible.protocol.solana.common.meta.TokenMetaParser
import com.rarible.protocol.solana.common.model.SolanaCollectionV1
import com.rarible.protocol.solana.common.model.SolanaCollectionV2
import com.rarible.protocol.solana.common.repository.CollectionRepository
import com.rarible.protocol.solana.common.service.CollectionConverter
import com.rarible.protocol.solana.nft.api.test.AbstractControllerTest
import com.rarible.protocol.solana.test.createRandomMetaplexMeta
import com.rarible.protocol.solana.test.createRandomMetaplexOffChainMeta
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class CollectionControllerFt : AbstractControllerTest() {

    @Autowired
    private lateinit var collectionConverter: CollectionConverter

    @Autowired
    private lateinit var collectionRepository: CollectionRepository

    @Test
    fun `collection v1`() = runBlocking<Unit> {
        val collectionV1 = SolanaCollectionV1(randomString(), randomString(), randomString())
        collectionRepository.save(collectionV1)
        val result = collectionControllerApi.getCollectionById(collectionV1.id).awaitFirst()
        assertThat(result).isEqualTo(collectionConverter.convertV1(collectionV1))
    }

    @Test
    fun `collection v2`() = runBlocking<Unit> {
        val collectionNft = createRandomMetaplexMeta()
        val metaOffChain = createRandomMetaplexOffChainMeta().copy(tokenAddress = collectionNft.tokenAddress)
        val collectionV2 = SolanaCollectionV2(collectionNft.tokenAddress)

        metaplexMetaRepository.save(collectionNft)
        metaplexOffChainMetaRepository.save(metaOffChain)

        val tokenMeta = TokenMetaParser.mergeOnChainAndOffChainMeta(collectionNft.metaFields, metaOffChain.metaFields)
        val expected = collectionConverter.convertV2(collectionV2, tokenMeta)

        collectionRepository.save(collectionV2)
        val result = collectionControllerApi.getCollectionById(collectionV2.id).awaitFirst()
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `collectionV2 as token`() = runBlocking<Unit> {
        val tokenWithMeta = saveTokenWithMeta()
        val tokenMeta = tokenWithMeta.tokenMeta!!

        val result = collectionControllerApi.getCollectionById(tokenWithMeta.token.mint).awaitFirst()
        val collectionDto = collectionConverter.convertV2(SolanaCollectionV2(tokenWithMeta.token.mint), tokenMeta)

        assertThat(result).isEqualTo(collectionDto)
    }
}
