package com.rarible.protocol.solana.common.service

import com.rarible.core.test.data.randomString
import com.rarible.protocol.solana.AbstractIntegrationTest
import com.rarible.protocol.solana.common.model.SolanaCollectionV1
import com.rarible.protocol.solana.common.model.SolanaCollectionV2
import com.rarible.protocol.solana.test.createRandomMetaplexMeta
import com.rarible.protocol.solana.test.createRandomMetaplexOffChainMeta
import com.rarible.protocol.solana.test.randomMint
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class CollectionServiceIt : AbstractIntegrationTest() {

    @Autowired
    lateinit var collectionService: CollectionService

    @Test
    fun `save and get`() = runBlocking<Unit> {
        val v1 = SolanaCollectionV1(randomString(), randomString(), randomString())
        val v2 = SolanaCollectionV2(randomMint())

        collectionService.save(v1)
        collectionService.save(v2)

        assertThat(collectionService.findById(v1.id)).isEqualTo(v1)
        assertThat(collectionService.findById(v2.id)).isEqualTo(v2)
    }

    @Test
    fun `find all`() = runBlocking<Unit> {
        val c3 = collectionService.save(SolanaCollectionV1("3", randomString(), randomString()))
        val c2 = collectionService.save(SolanaCollectionV2("2"))
        val c1 = collectionService.save(SolanaCollectionV2("1"))

        val page1 = collectionService.findAll(null, 1)
        val page2 = collectionService.findAll("111", 2)

        assertThat(page1).hasSize(1)
        assertThat(page2).hasSize(2)

        assertThat(page1[0]).isEqualTo(c1)
        assertThat(page2[0]).isEqualTo(c2)
        assertThat(page2[1]).isEqualTo(c3)
    }

    @Test
    fun `update collection v1 - new collection`() = runBlocking<Unit> {

        val meta = createRandomMetaplexOffChainMeta()
        val collection = meta.metaFields.collection!!

        val expected = SolanaCollectionV1(
            id = collection.hash,
            name = collection.name,
            family = collection.family,
        )

        collectionService.updateCollectionV1(meta)
        val saved = collectionService.findById(collection.hash)

        assertThat(saved).isEqualTo(expected)
    }

    @Test
    fun `update collection v1 - collection exists`() = runBlocking<Unit> {

        val meta = createRandomMetaplexOffChainMeta()
        val collection = meta.metaFields.collection!!

        val exists = SolanaCollectionV1(collection.hash, randomString(), randomString())
        collectionService.save(exists)

        collectionService.updateCollectionV1(meta)
        val saved = collectionService.findById(collection.hash)

        // Should not be updated
        assertThat(saved).isEqualTo(exists)
    }

    @Test
    fun `update collection v2`() = runBlocking<Unit> {

        val meta = createRandomMetaplexMeta()
        val collectionAddress = meta.metaFields.collection?.address!!

        val expected = SolanaCollectionV2(collectionAddress)

        collectionService.updateCollectionV2(meta)
        val saved = collectionService.findById(collectionAddress)

        assertThat(saved).isEqualTo(expected)
    }

}