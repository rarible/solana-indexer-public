package com.rarible.protocol.solana.repository

import com.rarible.core.test.data.randomBigInt
import com.rarible.core.test.data.randomString
import com.rarible.protocol.solana.AbstractIntegrationTest
import com.rarible.protocol.solana.common.records.SolanaBalanceRecord
import com.rarible.protocol.solana.common.repository.RecordsBalanceRepository
import com.rarible.protocol.solana.test.randomSolanaLog
import com.rarible.solana.protocol.dto.ActivitySortDto
import com.rarible.solana.protocol.dto.ActivityTypeDto
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.ZonedDateTime

internal class RecordsBalanceRepositoryIt : AbstractIntegrationTest() {
    @Autowired
    private lateinit var recordsBalanceRepository: RecordsBalanceRepository

    @Test
    fun `type logic`() = runBlocking<Unit> {
        val mint = randomString()
        listOf(
            randomMint().copy(mint = mint),
            randomBurn().copy(mint = mint),
            randomIncome().copy(mint = mint),
            randomOutcome().copy(mint = mint)
        ).forEach { recordsBalanceRepository.save(it) }

        recordsBalanceRepository.findByItem(listOf(ActivityTypeDto.MINT), mint).toList().let { records ->
            assertEquals(1, records.size)
            assertThat(records.single()).isInstanceOf(SolanaBalanceRecord.MintToRecord::class.java)
        }

        recordsBalanceRepository.findByItem(listOf(ActivityTypeDto.BURN), mint).toList().let { records ->
            assertEquals(1, records.size)
            assertThat(records.single()).isInstanceOf(SolanaBalanceRecord.BurnRecord::class.java)
        }

        recordsBalanceRepository.findByItem(listOf(ActivityTypeDto.TRANSFER), mint).toList().let { records ->
            assertEquals(1, records.size)
            assertThat(records.single()).isInstanceOf(SolanaBalanceRecord.TransferIncomeRecord::class.java)
        }

        recordsBalanceRepository.findByItem(ActivityTypeDto.values().toList(), mint).toList().let { records ->
            assertEquals(3, records.size)
        }

        recordsBalanceRepository.findByItem(emptyList(), mint).toList().let { records ->
            assert(records.isEmpty())
        }
    }

    @Test
    fun `continuation and order`() = runBlocking<Unit> {
        val types = ActivityTypeDto.values().toList()
        val mint = randomString()
        val data = (1..3).map { randomMint().copy(mint = mint) } +
                (1..3).map { randomBurn().copy(mint = mint) } +
                (1..4).map { randomIncome().copy(mint = mint) }
        data.forEach { recordsBalanceRepository.save(it) }

        val c1 = recordsBalanceRepository.findByItem(types, mint, size = 4).toList().let { records ->
            assertEquals(4, records.size)
            assertThat(records.first().id > records.last().id)
            assertNotNull(records.last().id)
            records.last().id
        }

        val c2 = recordsBalanceRepository.findByItem(types, mint, continuation = c1, size = 4).toList().let { records ->
            assertEquals(4, records.size)
            assertThat(records.first().id > records.last().id)
            assertNotNull(records.last().id)
            records.last().id
        }

        recordsBalanceRepository.findByItem(types, mint, continuation = c2, size = 4).toList().let { records ->
            assertEquals(2, records.size)
            assertThat(records.first().id > records.last().id)
            assertNull(records.last().id)
        }

        val c3 = recordsBalanceRepository.findByItem(types, mint, null, 4, ActivitySortDto.EARLIEST_FIRST).toList()
            .let { records ->
                assertEquals(4, records.size)
                assertThat(records.first().id < records.last().id)
                assertNotNull(records.last().id)
                records.last().id
            }

        val c4 = recordsBalanceRepository.findByItem(types, mint, c3, 4, ActivitySortDto.EARLIEST_FIRST).toList()
            .let { records ->
                assertEquals(4, records.size)
                assertThat(records.first().id < records.last().id)
                assertNotNull(records.last().id)
                records.last().id
            }

        recordsBalanceRepository.findByItem(types, mint, c4, 4, ActivitySortDto.EARLIEST_FIRST).toList()
            .let { records ->
                assertEquals(2, records.size)
                assertThat(records.first().id < records.last().id)
                assertNull(records.last().id)
            }
    }

    companion object {
        private val baseTimestamp = ZonedDateTime.parse("2022-01-03T22:26:07.000+00:00").toInstant()

        private fun randomMint() = SolanaBalanceRecord.MintToRecord(
            mintAmount = randomBigInt(),
            mint = randomString(),
            account = randomString(),
            log = randomSolanaLog(),
            timestamp = baseTimestamp,
        )

        private fun randomBurn() = SolanaBalanceRecord.BurnRecord(
            burnAmount = randomBigInt(),
            mint = randomString(),
            account = randomString(),
            log = randomSolanaLog(),
            timestamp = baseTimestamp,
        )

        private fun randomIncome() = SolanaBalanceRecord.TransferIncomeRecord(
            from = randomString(),
            owner = randomString(),
            mint = randomString(),
            incomeAmount = randomBigInt(),
            log = randomSolanaLog(),
            timestamp = baseTimestamp,
        )

        private fun randomOutcome() = SolanaBalanceRecord.TransferOutcomeRecord(
            to = randomString(),
            owner = randomString(),
            mint = randomString(),
            outcomeAmount = randomBigInt(),
            log = randomSolanaLog(),
            timestamp = baseTimestamp,
        )
    }
}
