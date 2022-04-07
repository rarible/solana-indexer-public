package com.rarible.protocol.solana.nft.listener.service.subscribers

import com.rarible.blockchain.scanner.solana.client.SolanaBlockchainLog
import com.rarible.core.test.data.randomString
import com.rarible.protocol.solana.common.records.OrderDirection
import com.rarible.protocol.solana.nft.listener.test.data.randomBuyInstruction
import com.rarible.protocol.solana.nft.listener.test.data.randomExecuteSaleInstruction
import com.rarible.protocol.solana.nft.listener.test.data.randomSaleInstruction
import com.rarible.protocol.solana.nft.listener.test.data.randomSolanaBlockchainBlock
import com.rarible.protocol.solana.test.randomSolanaLog
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AuctionHouseOrderExecuteSaleSubscriberTest {

    private val subscriber = AuctionHouseOrderExecuteSaleSubscriber()

    @Test
    fun `empty amount`() = runBlocking<Unit> {
        val log = SolanaBlockchainLog(randomSolanaLog(), randomExecuteSaleInstruction(size = 0))
        val block = randomSolanaBlockchainBlock(listOf(log))

        val result = subscriber.getEventRecords(block, log)

        // Should be filtered due to zero amount
        assertThat(result).hasSize(0)
    }

    @Test
    fun `not a execute sale log`() = runBlocking<Unit> {
        val saleLog = SolanaBlockchainLog(randomSolanaLog(), randomSaleInstruction())
        val block = randomSolanaBlockchainBlock(listOf(saleLog))

        val result = subscriber.getEventRecords(block, saleLog)

        assertThat(result).hasSize(0)
    }

    @Test
    fun `single record`() = runBlocking<Unit> {
        val log = SolanaBlockchainLog(randomSolanaLog(), randomExecuteSaleInstruction())
        val block = randomSolanaBlockchainBlock(listOf(log))

        val result = subscriber.getEventRecords(block, log)

        assertThat(result).hasSize(2)
        assertThat(result[0].direction).isEqualTo(OrderDirection.SELL)
        assertThat(result[1].direction).isEqualTo(OrderDirection.BUY)
    }

    @Test
    fun `with buy record`() = runBlocking<Unit> {
        val log = randomSolanaLog()
        val buyer = randomString()
        val buyLog = SolanaBlockchainLog(log, randomBuyInstruction(maker = buyer))
        val executeSaleLog = SolanaBlockchainLog(log, randomExecuteSaleInstruction(buyer = buyer))

        val block = randomSolanaBlockchainBlock(listOf(buyLog, executeSaleLog))

        val result = subscriber.getEventRecords(block, executeSaleLog)

        // Buy record here is adhoc, only SELL should be returned
        assertThat(result).hasSize(1)
        assertThat(result[0].direction).isEqualTo(OrderDirection.SELL)
    }

    @Test
    fun `with sell record`() = runBlocking<Unit> {
        val log = randomSolanaLog()
        val seller = randomString()
        val saleLog = SolanaBlockchainLog(log, randomSaleInstruction(maker = seller))
        val executeSaleLog = SolanaBlockchainLog(log, randomExecuteSaleInstruction(seller = seller))

        val block = randomSolanaBlockchainBlock(listOf(saleLog, executeSaleLog))

        val result = subscriber.getEventRecords(block, executeSaleLog)

        // Sale record here is adhoc, only BUY should be returned
        assertThat(result).hasSize(1)
        assertThat(result[0].direction).isEqualTo(OrderDirection.BUY)
    }

}