package com.rarible.protocol.solana.repository

import com.rarible.core.test.data.randomBigInt
import com.rarible.core.test.data.randomString
import com.rarible.protocol.solana.AbstractIntegrationTest
import com.rarible.protocol.solana.common.records.SolanaBalanceRecord
import com.rarible.protocol.solana.common.repository.RecordsBalanceRepository
import com.rarible.protocol.solana.dto.ActivitySortDto
import com.rarible.protocol.solana.dto.ActivityTypeDto
import com.rarible.protocol.solana.test.randomSolanaLog
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.ZonedDateTime

internal class RecordsBalanceRepositoryIt : AbstractIntegrationTest() {

    @Autowired
    private lateinit var recordsBalanceRepository: RecordsBalanceRepository

    @Test
    fun `type logic`() = runBlocking<Unit> {

        val mint = randomString()

        val mintRecord = randomMint().copy(mint = mint, timestamp = baseTimestamp.plusSeconds(1))
        val mintRecordOther = randomMint()
        val transferRecord = randomIncome().copy(mint = mint, timestamp = baseTimestamp.plusSeconds(3))
        val transferRecordOther = randomMint()
        val burnRecord = randomBurn().copy(mint = mint, timestamp = baseTimestamp.plusSeconds(5))
        val burnRecordOther = randomMint()

        recordsBalanceRepository.save(mintRecord)
        recordsBalanceRepository.save(mintRecordOther)
        recordsBalanceRepository.save(transferRecord)
        recordsBalanceRepository.save(transferRecordOther)
        recordsBalanceRepository.save(burnRecord)
        recordsBalanceRepository.save(burnRecordOther)

        findByItem(listOf(ActivityTypeDto.MINT), mint).toList().let { records ->
            assertEquals(1, records.size)
            assertThat(records.single()).isEqualTo(mintRecord)
        }

        findByItem(listOf(ActivityTypeDto.BURN), mint).toList().let { records ->
            assertEquals(1, records.size)
            assertThat(records.single()).isEqualTo(burnRecord)
        }

        findByItem(listOf(ActivityTypeDto.TRANSFER), mint).toList().let { records ->
            assertEquals(1, records.size)
            assertThat(records.single()).isEqualTo(transferRecord)
        }

        findByItem(ActivityTypeDto.values().toList(), mint).toList().let { records ->
            assertEquals(3, records.size)
        }

        findByItem(emptyList(), mint).toList().let { records ->
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

        val c1 = findByItem(types, mint, size = 4).toList().let { records ->
            println("\n${records.joinToString("\n") { "${it.timestamp}_${it.id}" }}")
            assertEquals(4, records.size)
            assertThat(records.first().id > records.last().id)
            assertNotNull(records.last().id)
            makeContinuation(records.last())
        }

        val c2 = findByItem(types, mint, continuation = c1, size = 4).toList().let { records ->
            println("\n${records.joinToString("\n") { "${it.timestamp}_${it.id}" }}")
            assertEquals(4, records.size)
            assertThat(records.first().id > records.last().id)
            assertNotNull(records.last().id)
            makeContinuation(records.last())
        }

        findByItem(types, mint, continuation = c2, size = 4).toList().let { records ->
            println("\n${records.joinToString("\n") { "${it.timestamp}_${it.id}" }}")
            assertEquals(2, records.size)
            assertThat(records.first().id > records.last().id)
        }

        val c3 = findByItem(types, mint, null, 4, ActivitySortDto.EARLIEST_FIRST).toList()
            .let { records ->
                println("\n${records.joinToString("\n") { "${it.timestamp}_${it.id}" }}")
                assertEquals(4, records.size)
                assertThat(records.first().id < records.last().id)
                assertNotNull(records.last().id)
                makeContinuation(records.last())
            }

        val c4 = findByItem(types, mint, c3, 4, ActivitySortDto.EARLIEST_FIRST).toList()
            .let { records ->
                println("\n${records.joinToString("\n") { "${it.timestamp}_${it.id}" }}")
                assertEquals(4, records.size)
                assertThat(records.first().id < records.last().id)
                assertNotNull(records.last().id)
                makeContinuation(records.last())
            }

        findByItem(types, mint, c4, 4, ActivitySortDto.EARLIEST_FIRST).toList()
            .let { records ->
                println("\n${records.joinToString("\n") { "${it.timestamp}_${it.id}" }}")
                assertEquals(2, records.size)
                assertThat(records.first().id < records.last().id)
            }
    }

    fun findByItem(
        type: Collection<ActivityTypeDto>,
        tokenAddress: String,
        continuation: String? = null,
        size: Int = 50,
        sort: ActivitySortDto = ActivitySortDto.LATEST_FIRST,
    ) = recordsBalanceRepository.findByItem(type, tokenAddress, continuation, size, sort)

    companion object {

        private val baseTimestamp = ZonedDateTime.parse("2022-01-03T22:26:07.000+00:00").toInstant()

        private fun makeContinuation(last: SolanaBalanceRecord) = "${last.timestamp.toEpochMilli()}_${last.id}"

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
    }
}
