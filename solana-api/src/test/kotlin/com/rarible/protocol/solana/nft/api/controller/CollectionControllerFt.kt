package com.rarible.protocol.solana.nft.api.controller

import com.rarible.core.test.data.randomString
import com.rarible.protocol.solana.common.model.SolanaCollectionV1
import com.rarible.protocol.solana.common.model.SolanaCollectionV2
import com.rarible.protocol.solana.common.repository.CollectionRepository
import com.rarible.protocol.solana.common.service.CollectionConverter
import com.rarible.protocol.solana.dto.CollectionsByIdRequestDto
import com.rarible.protocol.solana.nft.api.test.AbstractControllerTest
import com.rarible.protocol.solana.test.createRandomMetaplexMeta
import com.rarible.protocol.solana.test.createRandomMetaplexOffChainMeta
import com.rarible.protocol.solana.test.randomMint
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
        assertThat(result).isEqualTo(collectionConverter.toDto(collectionV1))
    }

    @Test
    fun `collection v2`() = runBlocking<Unit> {
        val collectionMint = randomMint()
        val collectionNft = createRandomMetaplexMeta(mint = collectionMint)
        val metaOffChain = createRandomMetaplexOffChainMeta(mint = collectionMint)
        val collectionV2 = SolanaCollectionV2(collectionMint)

        metaplexMetaRepository.save(collectionNft)
        metaplexOffChainMetaRepository.save(metaOffChain)
        collectionRepository.save(collectionV2)

        val expected = collectionConverter.toDto(SolanaCollectionV2(collectionMint))
        val result = collectionControllerApi.getCollectionById(collectionV2.id).awaitFirst()
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `search by ids`() = runBlocking<Unit> {
        val c1 = SolanaCollectionV1(randomString(), randomString(), randomString())
        collectionRepository.save(c1)
        val c2 = SolanaCollectionV1(randomString(), randomString(), randomString())
        collectionRepository.save(c2)
        val expected1 = collectionConverter.toDto(c1)
        val expected2 = collectionConverter.toDto(c2)

        val actual = collectionControllerApi.searchCollectionsByIds(
            CollectionsByIdRequestDto(listOf(c1.id, c2.id))
        ).awaitFirst()

        assertThat(actual.collections).containsExactlyInAnyOrder(expected1, expected2)
        assertThat(actual.continuation).isNull()
    }
}
