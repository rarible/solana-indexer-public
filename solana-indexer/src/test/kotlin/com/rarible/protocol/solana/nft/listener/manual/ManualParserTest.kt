package com.rarible.protocol.solana.nft.listener.manual

import com.rarible.blockchain.scanner.solana.client.SolanaClient
import com.rarible.blockchain.scanner.solana.client.SolanaHttpRpcApi
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

    private val client = SolanaClient(
        SolanaHttpRpcApi(
            urls = listOf("https://api.mainnet-beta.solana.com"),
            timeoutMillis = 10000
        ),
        programIds = emptySet() // All programs.
    )

    @Test
    fun `parse block log records`() = runBlocking<Unit> {
        val block = client.getBlock(98813350) ?: return@runBlocking
        for (subscriber in subscribers) {
            for (log in block.logs) {
                if (log.instruction.programId != subscriber.getDescriptor().programId) {
                    continue
                }
                println("  For ${subscriber.getDescriptor().id} and $log")
                val records = subscriber.getEventRecords(block, log)
                println("    Records [$records]")
            }
        }
    }
}
