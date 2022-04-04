package com.rarible.protocol.solana.repository

import com.rarible.protocol.solana.AbstractIntegrationTest
import com.rarible.protocol.solana.common.repository.SolanaAuctionHouseOrderRecordsRepository
import com.rarible.protocol.solana.test.OrderRecordDataFactory
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.query.Criteria

class SolanaAuctionHouseOrderRecordsRepositoryIt : AbstractIntegrationTest() {

    @Autowired
    private lateinit var orderRecordsRepository: SolanaAuctionHouseOrderRecordsRepository

    @Test
    fun `save and find record`() = runBlocking<Unit> {
        val record = OrderRecordDataFactory.randomBuyRecord()
        orderRecordsRepository.save(record)

        val result = orderRecordsRepository.findBy(Criteria(), size = 50).toList()

        assertEquals(record, result.single())
    }

    @Test
    fun `records order`() = runBlocking<Unit> {
        val records = listOf(
            OrderRecordDataFactory.randomBuyRecord(),
            OrderRecordDataFactory.randomCancel(),
            OrderRecordDataFactory.randomSellRecord(),
            OrderRecordDataFactory.randomExecuteSaleRecord(),
        )
        records.forEach { orderRecordsRepository.save(it) }

        val ascResult = orderRecordsRepository.findBy(Criteria(), asc = true, size = 50).toList()
        val ascExpected = records.sortedWith { a, b -> a.id.compareTo(b.id) }
        assertEquals(ascExpected, ascResult)

        val descResult = orderRecordsRepository.findBy(Criteria(), asc = false, size = 50).toList()
        val descExpected = records.sortedWith { a, b -> -a.id.compareTo(b.id) }
        assertEquals(descExpected, descResult)
    }
}
