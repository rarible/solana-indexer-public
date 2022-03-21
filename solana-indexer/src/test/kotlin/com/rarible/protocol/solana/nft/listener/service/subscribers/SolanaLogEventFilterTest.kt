package com.rarible.protocol.solana.nft.listener.service.subscribers

import com.rarible.blockchain.scanner.framework.data.BlockEvent
import com.rarible.blockchain.scanner.framework.data.LogEvent
import com.rarible.blockchain.scanner.solana.model.SolanaDescriptor
import com.rarible.blockchain.scanner.solana.model.SolanaLogRecord
import com.rarible.core.test.data.randomString
import com.rarible.protocol.solana.common.configuration.FeatureFlags
import com.rarible.protocol.solana.common.records.SolanaTokenRecord
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

    private val descriptor: SolanaDescriptor = object : SolanaDescriptor(
        programId = SolanaProgramId.SPL_TOKEN_PROGRAM,
        id = "test_descriptor",
        groupId = SubscriberGroup.TOKEN.id,
        entityType = SolanaTokenRecord.InitializeMintRecord::class.java,
        collection = SubscriberGroup.TOKEN.collectionName
    ) {}

    private val filter = SolanaBalanceLogEventFilter(accountToMintAssociationService, FeatureFlags())

    @BeforeEach
    fun beforeEach() {
        clearMocks(accountToMintAssociationService)
    }

    @Test
    fun `not a base solana record`() = runBlocking<Unit> {
        val record: SolanaLogRecord = mockk()
        val event = randomLogEvent(record)

        coEvery { accountToMintAssociationService.saveMintsByAccounts(any()) } returns Unit
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

        // Mapped mints should be requested only for all transfers
        coEvery {
            accountToMintAssociationService.getMintsByAccounts(
                mutableSetOf(transferWithInitMint.to, transferWithMapping.from, transferWithoutMapping.to)
            )
        } returns mapOf(transferWithMapping.from to mappedMint)

        // Mapping from Init event should be stored in cache
        coEvery {
            accountToMintAssociationService.saveMintsByAccounts(mapOf(init.balanceAccount to init.mint))
        } returns Unit

        // Event with currency token should be ignored
        coEvery { accountToMintAssociationService.isCurrencyToken(initMint) } returns true
        coEvery { accountToMintAssociationService.isCurrencyToken(mappedMint) } returns false

        val event = randomLogEvent(init, transferWithInitMint, transferWithMapping, transferWithoutMapping)
        val result = filter.filter(listOf(event))

        val records = getRecords(result)

        assertThat(records).hasSize(2)
        // Passed since type of mint is not a currency, mint for transfer updated
        assertThat(records[0]).isEqualTo(transferWithMapping.copy(mint = mappedMint))
        // Passed since type of mint not determined, mint not updated since it is not found
        assertThat(records[1]).isEqualTo(transferWithoutMapping)
    }

    @Test
    fun `transfer with mint`() = runBlocking<Unit> {
        val incomeMint = randomString()
        val outcomeMint = randomString()
        val incomeTransfer = randomBalanceIncomeTransfer().copy(mint = incomeMint)
        val outcomeTransfer = randomBalanceOutcomeTransfer().copy(mint = outcomeMint)

        // We're requesting balances in any way in order to determine - should we write new mapping in DB or not
        coEvery {
            accountToMintAssociationService.getMintsByAccounts(mutableSetOf(incomeTransfer.to, outcomeTransfer.from))
        } returns mapOf()

        // Mapping from transfer with known mint should be stored in cache
        coEvery {
            accountToMintAssociationService.saveMintsByAccounts(
                mapOf(
                    incomeTransfer.to to incomeMint,
                    outcomeTransfer.from to outcomeMint
                )
            )
        } returns Unit

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
