package com.rarible.protocol.solana.nft.listener.service.subscribers

import com.rarible.blockchain.scanner.solana.client.SolanaBlockchainLog
import com.rarible.protocol.solana.common.records.OrderDirection
import com.rarible.protocol.solana.nft.listener.test.data.randomBuyInstruction
import com.rarible.protocol.solana.nft.listener.test.data.randomCancelInstruction
import com.rarible.protocol.solana.nft.listener.test.data.randomSolanaBlockchainBlock
import com.rarible.protocol.solana.test.randomSolanaLog
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AuctionHouseOrderCancelSubscriberTest {

    private val subscriber = AuctionHouseOrderCancelSubscriber()

    @Test
    fun `not a cancel log`() = runBlocking<Unit> {
        val buyLog = SolanaBlockchainLog(randomSolanaLog(), randomBuyInstruction())
        val block = randomSolanaBlockchainBlock(listOf(buyLog))

        val result = subscriber.getEventRecords(block, buyLog)

        assertThat(result).hasSize(0)
    }

    @Test
    fun `single record`() = runBlocking<Unit> {
        val cancelLog = SolanaBlockchainLog(randomSolanaLog(), randomCancelInstruction())
        val block = randomSolanaBlockchainBlock(listOf(cancelLog))

        val result = subscriber.getEventRecords(block, cancelLog)

        assertThat(result).hasSize(2)
        assertThat(result[0].direction).isEqualTo(OrderDirection.BUY)
        assertThat(result[1].direction).isEqualTo(OrderDirection.SELL)
    }

}