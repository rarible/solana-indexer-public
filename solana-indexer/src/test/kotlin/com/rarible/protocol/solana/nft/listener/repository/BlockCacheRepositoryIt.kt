package com.rarible.protocol.solana.nft.listener.repository

import com.rarible.protocol.solana.nft.listener.AbstractBlockScannerTest
import com.rarible.protocol.solana.nft.listener.block.cache.BlockCacheRepository
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import scalether.util.Hex
import kotlin.random.Random

class BlockCacheRepositoryIt : AbstractBlockScannerTest() {
    @Autowired
    private lateinit var blockCacheRepository: BlockCacheRepository

    @Test
    fun `blockCacheRepository save and find work`() = runBlocking<Unit> {
        val id = Random.nextLong()
        val bytes = Random.nextBytes(32)
        blockCacheRepository.save(id, bytes)
        val found = blockCacheRepository.find(id)
        assertThat(Hex.to(found)).isEqualTo(Hex.to(bytes))
    }
}