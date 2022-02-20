package com.rarible.protocol.solana.repository

import com.rarible.protocol.solana.AbstractIntegrationTest
import com.rarible.protocol.solana.common.repository.MetaplexMetaRepository
import com.rarible.protocol.solana.test.createRandomMetaplexMeta
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class MetaplexMetaRepositoryIt : AbstractIntegrationTest() {

    @Autowired
    private lateinit var metaplexMetaRepository: MetaplexMetaRepository

    @Test
    fun `save and find by meta address`() = runBlocking<Unit> {
        val metaplexMeta = createRandomMetaplexMeta()
        metaplexMetaRepository.save(metaplexMeta)
        assertThat(metaplexMetaRepository.findByMetaAddress(metaplexMeta.metaAddress)).isEqualTo(metaplexMeta)
    }

    @Test
    fun `save and find by meta collection address`() = runBlocking<Unit> {
        val metaplexMeta = createRandomMetaplexMeta()
        val metaplexMeta2 = createRandomMetaplexMeta().let {
            it.copy(metaFields = it.metaFields.copy(collection = metaplexMeta.metaFields.collection))
        }
        val metaplexMeta3 = createRandomMetaplexMeta()
        metaplexMetaRepository.save(metaplexMeta)
        metaplexMetaRepository.save(metaplexMeta2)
        metaplexMetaRepository.save(metaplexMeta3)
        assertThat(metaplexMetaRepository.findByCollectionAddress(metaplexMeta.metaFields.collection!!.address).toList())
            .isEqualTo(listOf(metaplexMeta, metaplexMeta2).sortedBy { it.metaAddress })
    }

    @Test
    fun `save and find by token address`() = runBlocking<Unit> {
        val metaplexMeta = createRandomMetaplexMeta()
        metaplexMetaRepository.save(metaplexMeta)
        assertThat(metaplexMetaRepository.findByTokenAddress(metaplexMeta.tokenAddress)).isEqualTo(metaplexMeta)
    }
}
