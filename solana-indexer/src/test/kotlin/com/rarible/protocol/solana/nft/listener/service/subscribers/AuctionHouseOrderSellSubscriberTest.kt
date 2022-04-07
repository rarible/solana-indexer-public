package com.rarible.protocol.solana.nft.listener.service.subscribers

import com.rarible.blockchain.scanner.solana.client.SolanaBlockchainLog
import com.rarible.core.test.data.randomString
import com.rarible.protocol.solana.nft.listener.test.data.randomBuyInstruction
import com.rarible.protocol.solana.nft.listener.test.data.randomExecuteSaleInstruction
import com.rarible.protocol.solana.nft.listener.test.data.randomSaleInstruction
import com.rarible.protocol.solana.nft.listener.test.data.randomSolanaBlockchainBlock
import com.rarible.protocol.solana.test.randomSolanaLog
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AuctionHouseOrderSellSubscriberTest {

    private val subscriber = AuctionHouseOrderSellSubscriber()

    @Test
    fun `empty amount`() = runBlocking<Unit> {
        val saleLog = SolanaBlockchainLog(randomSolanaLog(), randomSaleInstruction(size = 0))
        val block = randomSolanaBlockchainBlock(listOf(saleLog))

        val result = subscriber.getEventRecords(block, saleLog)

        // Should be filtered due to zero amount
        assertThat(result).hasSize(0)
    }

    @Test
    fun `not a sale log`() = runBlocking<Unit> {
        val buyLog = SolanaBlockchainLog(randomSolanaLog(), randomBuyInstruction())
        val block = randomSolanaBlockchainBlock(listOf(buyLog))

        val result = subscriber.getEventRecords(block, buyLog)

        assertThat(result).hasSize(0)
    }

    @Test
    fun `single record`() = runBlocking<Unit> {
        val saleLog = SolanaBlockchainLog(randomSolanaLog(), randomSaleInstruction())
        val block = randomSolanaBlockchainBlock(listOf(saleLog))

        val result = subscriber.getEventRecords(block, saleLog)

        assertThat(result).hasSize(1)
        assertThat(result[0].log).isEqualTo(saleLog.log)
    }

    @Test
    fun `with execute sale record`() = runBlocking<Unit> {
        val log = randomSolanaLog()
        val seller = randomString()
        val saleLog = SolanaBlockchainLog(log, randomSaleInstruction(maker = seller))
        val executeSaleLog = SolanaBlockchainLog(log, randomExecuteSaleInstruction(seller = seller))

        val block = randomSolanaBlockchainBlock(listOf(saleLog, executeSaleLog))

        val result = subscriber.getEventRecords(block, saleLog)

        // Nothing should be returned, ExecuteSale for order present in Block
        assertThat(result).hasSize(0)
    }

    @Test
    fun `with execute sale records - not matched`() = runBlocking<Unit> {
        val log = randomSolanaLog()
        val seller = randomString()
        val saleLog = SolanaBlockchainLog(log, randomSaleInstruction(maker = seller))
        // Same log, but different seller
        val executeSaleLog1 = SolanaBlockchainLog(log, randomExecuteSaleInstruction())
        // Same seller, but in another log
        val executeSaleLog2 = SolanaBlockchainLog(randomSolanaLog(), randomExecuteSaleInstruction(seller = seller))

        val block = randomSolanaBlockchainBlock(listOf(saleLog, executeSaleLog1, executeSaleLog2))

        val result = subscriber.getEventRecords(block, saleLog)

        // Related ExecuteSale not found, sale record should be returned
        assertThat(result).hasSize(1)
    }

}