package com.rarible.protocol.solana.nft.listener.manual

import com.rarible.protocol.solana.nft.listener.AbstractBlockScannerTest
import com.rarible.protocol.solana.nft.listener.block.cache.BlockCacheTaskHandler
import com.rarible.protocol.solana.nft.listener.block.cache.BlockCacheClient
import com.rarible.protocol.solana.nft.listener.block.cache.BlockCacheProperties
import com.rarible.protocol.solana.nft.listener.block.cache.BlockCacheRepository
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

@Disabled
class ManualBlockCacheTaskTest : AbstractBlockScannerTest() {
    @Autowired
    private lateinit var repository: BlockCacheRepository

    @Test
    fun `block task works`() = runBlocking {
        val client = BlockCacheClient(
            urls = listOf("https://holy-proud-wave.solana-mainnet.quiknode.pro/790699a8dbe2e4f3b6b5593a366664d78646cf95/")
        )
        val handler = BlockCacheTaskHandler(
            client = client,
            repository = repository,
            blockCacheProperties = BlockCacheProperties(mongo = null),
            meterRegistry = SimpleMeterRegistry()
        )
        handler.runLongTask(10, "20").collect()

        (10L..20L).forEach {
            val found = repository.find(it)
            assertThat(found).isNotNull()
            println("block#$it = ${String(found!!)}")
        }
    }
}