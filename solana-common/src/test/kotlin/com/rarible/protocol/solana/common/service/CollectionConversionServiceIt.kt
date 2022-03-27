package com.rarible.protocol.solana.common.service

import com.rarible.core.test.data.randomString
import com.rarible.protocol.solana.AbstractIntegrationTest
import com.rarible.protocol.solana.common.converter.CollectionConverter
import com.rarible.protocol.solana.common.model.SolanaCollectionV1
import com.rarible.protocol.solana.common.model.SolanaCollectionV2
import com.rarible.protocol.solana.common.repository.MetaplexMetaRepository
import com.rarible.protocol.solana.common.repository.MetaplexOffChainMetaRepository
import com.rarible.protocol.solana.dto.CollectionDto
import com.rarible.protocol.solana.test.createRandomMetaplexMeta
import com.rarible.protocol.solana.test.createRandomMetaplexOffChainMeta
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class CollectionConversionServiceIt : AbstractIntegrationTest() {

    @Autowired
    lateinit var collectionConversionService: CollectionConversionService

    @Autowired
    lateinit var metaplexMetaRepository: MetaplexMetaRepository

    @Autowired
    lateinit var metaplexOffChainMetaRepository: MetaplexOffChainMetaRepository

    @Test
    fun `to dto - f1`() = runBlocking<Unit> {
        val collection = SolanaCollectionV1(randomString(), randomString(), randomString())

        val expected = CollectionDto(address = collection.id, name = collection.name)

        val dto = collectionConversionService.toDto(collection)

        assertThat(dto).isEqualTo(expected)
    }

    @Test
    fun `to dto - v2 without offchain meta`() = runBlocking<Unit> {
        val meta = createRandomMetaplexMeta()
        val collection = SolanaCollectionV2(meta.tokenAddress)

        metaplexMetaRepository.save(meta)

        val expected = CollectionConverter.convertV2(collection, meta.metaFields, null)

        val dto = collectionConversionService.toDto(collection)

        assertThat(dto).isEqualTo(expected)
    }

    @Test
    fun `to dto - v2 with offchain meta`() = runBlocking<Unit> {
        val meta = createRandomMetaplexMeta()
        val metaOff = createRandomMetaplexOffChainMeta().copy(tokenAddress = meta.tokenAddress)
        val collection = SolanaCollectionV2(meta.tokenAddress)

        metaplexMetaRepository.save(meta)
        metaplexOffChainMetaRepository.save(metaOff)

        val expected = CollectionConverter.convertV2(collection, meta.metaFields, metaOff.metaFields)

        val dto = collectionConversionService.toDto(collection)

        assertThat(dto).isEqualTo(expected)
    }

}