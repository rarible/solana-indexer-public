package com.rarible.protocol.solana.repository

import com.rarible.protocol.solana.AbstractIntegrationTest
import com.rarible.protocol.solana.common.repository.SolanaBalanceRecordsRepository
import com.rarible.protocol.solana.test.BalanceRecordDataFactory
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.query.Criteria

internal class SolanaBalanceRecordsRepositoryIt : AbstractIntegrationTest() {

    @Autowired
    private lateinit var balanceRecordsRepository: SolanaBalanceRecordsRepository

    @Test
    fun `save and find record`() = runBlocking<Unit> {
        val record = BalanceRecordDataFactory.randomMintToRecord()
        balanceRecordsRepository.save(record)

        val result = balanceRecordsRepository.findBy(Criteria()).toList()

        assertEquals(record, result.single())
    }

    @Test
    fun `records order`() = runBlocking<Unit> {
        val records = listOf(
            BalanceRecordDataFactory.randomMintToRecord(),
            BalanceRecordDataFactory.randomBurnRecord(),
            BalanceRecordDataFactory.randomIncomeRecord(),
            BalanceRecordDataFactory.randomOutcomeRecord(),
        )
        records.forEach { balanceRecordsRepository.save(it) }

        balanceRecordsRepository.findBy(Criteria(), asc = true).toList().let { result ->
            val expected = records.sortedWith { a, b ->
                if (a.timestamp == b.timestamp) a.id.compareTo(b.id) else a.timestamp.compareTo(b.timestamp)
            }

            assertEquals(expected, result)
        }

        balanceRecordsRepository.findBy(Criteria(), asc = false).toList().let { result ->
            val expected = records.sortedWith { a, b ->
                if (a.timestamp == b.timestamp) -a.id.compareTo(b.id) else -a.timestamp.compareTo(b.timestamp)
            }

            assertEquals(expected, result)
        }
    }
}

