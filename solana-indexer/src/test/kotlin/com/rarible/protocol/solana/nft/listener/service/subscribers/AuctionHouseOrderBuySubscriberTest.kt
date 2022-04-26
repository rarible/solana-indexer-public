package com.rarible.protocol.solana.nft.listener.service.subscribers

import com.rarible.blockchain.scanner.solana.client.SolanaBlockchainLog
import com.rarible.protocol.solana.nft.listener.test.data.randomBuyInstruction
import com.rarible.protocol.solana.nft.listener.test.data.randomExecuteSaleInstruction
import com.rarible.protocol.solana.nft.listener.test.data.randomSaleInstruction
import com.rarible.protocol.solana.nft.listener.test.data.randomSolanaBlockchainBlock
import com.rarible.protocol.solana.test.randomAccount
import com.rarible.protocol.solana.test.randomSolanaLog
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AuctionHouseOrderBuySubscriberTest {

    private val subscriber = AuctionHouseOrderBuySubscriber()

    @Test
    fun `empty amount`() = runBlocking<Unit> {
        val buyLog = SolanaBlockchainLog(randomSolanaLog(), randomBuyInstruction(size = 0))
        val block = randomSolanaBlockchainBlock(listOf(buyLog))

        val result = subscriber.getEventRecords(block, buyLog)

        // Should be filtered due to zero amount
        assertThat(result).hasSize(0)
    }

    @Test
    fun `not a buy log`() = runBlocking<Unit> {
        val saleLog = SolanaBlockchainLog(randomSolanaLog(), randomSaleInstruction())
        val block = randomSolanaBlockchainBlock(listOf(saleLog))

        val result = subscriber.getEventRecords(block, saleLog)

        assertThat(result).hasSize(0)
    }

    @Test
    fun `single record`() = runBlocking<Unit> {
        val saleLog = SolanaBlockchainLog(randomSolanaLog(), randomBuyInstruction())
        val block = randomSolanaBlockchainBlock(listOf(saleLog))

        val sellRecords = subscriber.getEventRecords(block, saleLog)

        assertThat(sellRecords).hasSize(1)
        assertThat(sellRecords[0].log).isEqualTo(saleLog.log)
    }

    @Test
    fun `with execute sale record`() = runBlocking<Unit> {
        val log = randomSolanaLog()
        val buyer = randomAccount()
        val buyLog = SolanaBlockchainLog(log, randomBuyInstruction(maker = buyer))
        val executeSaleLog = SolanaBlockchainLog(log, randomExecuteSaleInstruction(buyer = buyer))

        val block = randomSolanaBlockchainBlock(listOf(buyLog, executeSaleLog))

        val result = subscriber.getEventRecords(block, buyLog)

        // Nothing should be returned, ExecuteSale for order present in Block
        assertThat(result).hasSize(0)
    }

    @Test
    fun `with execute sale records - not matched`() = runBlocking<Unit> {
        val log = randomSolanaLog()
        val buyer = randomAccount()
        val buyLog = SolanaBlockchainLog(log, randomBuyInstruction(maker = buyer))
        // Same log, but different buyer
        val executeSaleLog1 = SolanaBlockchainLog(log, randomExecuteSaleInstruction())
        // Same buyer, but in another log
        val executeSaleLog2 = SolanaBlockchainLog(randomSolanaLog(), randomExecuteSaleInstruction(buyer = buyer))

        val block = randomSolanaBlockchainBlock(listOf(buyLog, executeSaleLog1, executeSaleLog2))

        val result = subscriber.getEventRecords(block, buyLog)

        // Related ExecuteSale not found, buy record should be returned
        assertThat(result).hasSize(1)
    }

}