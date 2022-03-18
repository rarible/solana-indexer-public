package com.rarible.protocol.solana.nft.listener.service.subscribers

import com.rarible.blockchain.scanner.framework.data.BlockEvent
import com.rarible.blockchain.scanner.framework.data.LogEvent
import com.rarible.blockchain.scanner.solana.model.SolanaDescriptor
import com.rarible.blockchain.scanner.solana.model.SolanaLogRecord
import com.rarible.core.test.data.randomString
import com.rarible.protocol.solana.nft.listener.service.AccountToMintAssociationService
import com.rarible.protocol.solana.nft.listener.test.data.randomBalanceIncomeTransfer
import com.rarible.protocol.solana.nft.listener.test.data.randomBalanceInitRecord
import com.rarible.protocol.solana.nft.listener.test.data.randomBalanceOutcomeTransfer
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SolanaLogEventFilterTest {

    private val accountToMintAssociationService: AccountToMintAssociationService = mockk()
    private val blockEvent: BlockEvent<*> = mockk()
    private val descriptor: SolanaDescriptor = mockk()

    private val filter = SolanaBalanceLogEventFilter(accountToMintAssociationService)

    @BeforeEach
    fun beforeEach() {
        clearMocks(accountToMintAssociationService)
    }

    @Test
    fun `not a base solana record`() = runBlocking<Unit> {
        val record: SolanaLogRecord = mockk()
        val event = randomLogEvent(record)

        coEvery { accountToMintAssociationService.saveAccountToMintMapping(any()) } returns Unit
        coEvery { accountToMintAssociationService.getMintsByAccounts(any()) } returns emptyMap()

        val result = filter.filter(listOf(event))

        assertThat(result).hasSize(1)

        val records = getRecords(result)
        assertThat(records).hasSize(1)
        assertThat(records[0]).isEqualTo(record)
    }

    @Test
    fun `transfer and init records`() = runBlocking<Unit> {
        val init = randomBalanceInitRecord()

        val transferWithInitMint = randomBalanceIncomeTransfer().copy(to = init.balanceAccount)
        val transferWithMapping = randomBalanceOutcomeTransfer()
        val transferWithoutMapping = randomBalanceIncomeTransfer()

        val initMint = init.mint
        val mappedMint = randomString()

        // Mapping from Init events should be stored in cache
        coEvery {
            accountToMintAssociationService.saveAccountToMintMapping(mapOf(init.balanceAccount to init.mint))
        } returns Unit

        // Mapped mints should be requested only for 2 transfers, for the first we already have mapping,
        // only for one mint mapping is found
        coEvery {
            accountToMintAssociationService.getMintsByAccounts(
                mutableSetOf(transferWithMapping.from, transferWithoutMapping.to)
            )
        } returns mapOf(transferWithMapping.from to mappedMint)

        // Event with currency token should be ignored
        coEvery { accountToMintAssociationService.isCurrencyToken(initMint) } returns true
        coEvery { accountToMintAssociationService.isCurrencyToken(mappedMint) } returns false

        val event = randomLogEvent(init, transferWithInitMint, transferWithMapping, transferWithoutMapping)
        val result = filter.filter(listOf(event))

        val records = getRecords(result)

        assertThat(records).hasSize(3)
        assertThat(records[0]).isEqualTo(init) // Passed since type of mint is not a currency
        assertThat(records[1]).isEqualTo(transferWithMapping) // Passed since type of mint is not a currency
        assertThat(records[2]).isEqualTo(transferWithoutMapping) // Passed since type of mint not determined
    }

    @Test
    fun `transfer with mint`() = runBlocking<Unit> {
        val incomeMint = randomString()
        val outcomeMint = randomString()
        val incomeTransfer = randomBalanceIncomeTransfer().copy(mint = incomeMint)
        val outcomeTransfer = randomBalanceIncomeTransfer().copy(mint = outcomeMint)

        // Mapping from transfer with known mint should be stored in cache
        coEvery {
            accountToMintAssociationService.saveAccountToMintMapping(
                mapOf(
                    incomeTransfer.to to incomeMint,
                    incomeTransfer.from to incomeMint,
                    outcomeTransfer.to to outcomeMint,
                    outcomeTransfer.from to outcomeMint
                )
            )
        } returns Unit

        // Nothing should be requested - we know mint for these transfers
        coEvery { accountToMintAssociationService.getMintsByAccounts(mutableSetOf()) } returns mapOf()

        // Events with currency token should be ignored
        coEvery { accountToMintAssociationService.isCurrencyToken(incomeMint) } returns true
        coEvery { accountToMintAssociationService.isCurrencyToken(outcomeMint) } returns true

        val event = randomLogEvent(incomeTransfer, outcomeTransfer)
        val result = filter.filter(listOf(event))

        val records = getRecords(result)
        assertThat(records).hasSize(0)

    }

    private fun randomLogEvent(vararg records: SolanaLogRecord): LogEvent<SolanaLogRecord, SolanaDescriptor> {
        return LogEvent(
            blockEvent = blockEvent,
            descriptor = descriptor,
            logRecordsToInsert = records.toList(),
            logRecordsToRemove = emptyList()
        )
    }

    private fun getRecords(logEvents: List<LogEvent<SolanaLogRecord, SolanaDescriptor>>): List<SolanaLogRecord> {
        return logEvents.map { it.logRecordsToInsert }.flatten()
    }
}