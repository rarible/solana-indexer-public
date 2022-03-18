package com.rarible.protocol.solana.nft.listener.manual

import com.rarible.blockchain.scanner.solana.client.SolanaClient
import com.rarible.blockchain.scanner.solana.subscriber.SolanaLogEventSubscriber
import com.rarible.protocol.solana.nft.listener.AbstractBlockScannerTest
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

@Disabled
class ManualParserTest : AbstractBlockScannerTest() {
    @Autowired
    private lateinit var subscribers: List<SolanaLogEventSubscriber>

    private val client = SolanaClient(listOf("https://white-damp-rain.solana-mainnet.quiknode.pro/728e275a5bf349a7384fcc8e72d463df65b24a8c/"))

    @Test
    fun `parse block log records`() = runBlocking<Unit> {
        val block = client.getBlock(114371726L) ?: return@runBlocking
        for (subscriber in subscribers) {
            for (log in block.logs) {
                val records = subscriber.getEventRecords(block, log)
                println("For log $log: [$records]")
            }
        }
    }
}
