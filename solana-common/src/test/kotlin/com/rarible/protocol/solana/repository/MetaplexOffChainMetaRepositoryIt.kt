package com.rarible.protocol.solana.repository

import com.rarible.protocol.solana.AbstractIntegrationTest
import com.rarible.protocol.solana.common.meta.MetaplexOffChainCollectionHash
import com.rarible.protocol.solana.common.repository.MetaplexOffChainMetaRepository
import com.rarible.protocol.solana.test.createRandomMetaplexOffChainMeta
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class MetaplexOffChainMetaRepositoryIt : AbstractIntegrationTest() {

    @Autowired
    private lateinit var metaplexOffChainMetaRepository: MetaplexOffChainMetaRepository

    @Test
    fun `save and find by token address`() = runBlocking<Unit> {
        val metaplexOffChainMeta = createRandomMetaplexOffChainMeta()
        metaplexOffChainMetaRepository.save(metaplexOffChainMeta)
        assertThat(metaplexOffChainMetaRepository.findByTokenAddress(metaplexOffChainMeta.tokenAddress))
            .isEqualTo(metaplexOffChainMeta)
    }

    @Test
    fun `save and find by off-chain collection hash`() = runBlocking<Unit> {
        val metaplexOffChainMeta = createRandomMetaplexOffChainMeta()
        metaplexOffChainMetaRepository.save(metaplexOffChainMeta)
        val collection = metaplexOffChainMeta.metaFields.collection!!
        val collectionHash = MetaplexOffChainCollectionHash.calculateCollectionHash(
            name = collection.name,
            family = collection.family,
            creators = metaplexOffChainMeta.metaFields.properties!!.creators!!.map { it.address }
        )

        assertThat(metaplexOffChainMetaRepository.findByOffChainCollectionHash(collectionHash).single())
            .isEqualTo(metaplexOffChainMeta)
    }
}
