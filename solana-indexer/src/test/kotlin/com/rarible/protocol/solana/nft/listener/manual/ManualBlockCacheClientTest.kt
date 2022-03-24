package com.rarible.protocol.solana.nft.listener.manual

import com.rarible.blockchain.scanner.solana.client.dto.GetBlockRequest
import com.rarible.protocol.solana.nft.listener.block.cache.BlockCacheClient
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

@Disabled
class ManualBlockCacheClientTest {
    @Test
    fun `BlockCacheClient gets block content`() = runBlocking {
        val client = BlockCacheClient(urls = listOf("https://holy-proud-wave.solana-mainnet.quiknode.pro/790699a8dbe2e4f3b6b5593a366664d78646cf95/"))
        val result = client.getBlock(114674863, GetBlockRequest.TransactionDetails.Full)
        println("result is " + String(result))
    }
}