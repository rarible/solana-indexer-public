package com.rarible.protocol.solana.nft.listener.service.subscribers.filter

import com.rarible.blockchain.scanner.framework.data.BlockEvent
import com.rarible.blockchain.scanner.framework.data.LogEvent
import com.rarible.blockchain.scanner.solana.model.SolanaDescriptor
import com.rarible.blockchain.scanner.solana.model.SolanaLogRecord
import com.rarible.core.test.data.randomString
import com.rarible.protocol.solana.common.records.SolanaTokenRecord
import com.rarible.protocol.solana.nft.listener.service.AccountToMintAssociationService
import com.rarible.protocol.solana.nft.listener.service.subscribers.SolanaProgramId
import com.rarible.protocol.solana.common.records.SubscriberGroup
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

    private val blackListedToken = randomString()

    private val filter = SolanaRecordsLogEventFilter(
        accountToMintAssociationService,
        SolanaBlackListTokenFilter(setOf(blackListedToken))
    )

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

        val transferWithInitMint = randomBalanceIncomeTransfer().copy(mint = "").copy(owner = init.balanceAccount)
        val transferWithMapping = randomBalanceOutcomeTransfer().copy(mint = "")
        val transferWithoutMapping = randomBalanceIncomeTransfer().copy(mint = "")

        val initMint = init.mint
        val mappedMint = randomString()

        // Mapped mints should be requested for all non-currency transfers
        coEvery {
            accountToMintAssociationService.getMintsByAccounts(
                mutableSetOf(
                    transferWithMapping.to,
                    transferWithMapping.owner,
                    transferWithoutMapping.owner,
                    transferWithoutMapping.from
                )
            )
        } returns mapOf(transferWithMapping.owner to mappedMint)

        // Mapping should be same only for non-existing non-currency associations
        coEvery {
            accountToMintAssociationService.saveMintsByAccounts(
                mapOf(
                    // for this transfer we know TO now, should be stored too
                    transferWithMapping.to to mappedMint,
                )
            )
        } returns Unit

        // Event with currency token should be ignored
        coEvery { accountToMintAssociationService.isCurrencyToken(initMint) } returns true
        coEvery { accountToMintAssociationService.isCurrencyToken(mappedMint) } returns false

        val event = randomLogEvent(init, transferWithInitMint, transferWithMapping, transferWithoutMapping)
        val result = filter.filter(listOf(event))

        val records = getRecords(result)

        assertThat(records).hasSize(1)
        // Passed since type of mint is not a currency, mint for transfer updated
        assertThat(records[0]).isEqualTo(transferWithMapping.copy(mint = mappedMint))
    }

    @Test
    fun `transfer with mint`() = runBlocking<Unit> {
        val incomeMint = randomString()
        val outcomeMint = randomString()
        val incomeTransfer = randomBalanceIncomeTransfer().copy(mint = incomeMint)
        val outcomeTransfer = randomBalanceOutcomeTransfer().copy(mint = outcomeMint)

        // We're requesting balances in any way in order to determine - should we write new mapping in DB or not
        coEvery {
            accountToMintAssociationService.getMintsByAccounts(
                mutableSetOf(
                    outcomeTransfer.to,
                    outcomeTransfer.owner
                )
            )
        } returns mapOf()

        // Mapping from transfer of NFT with known mint should be stored in cache
        coEvery {
            accountToMintAssociationService.saveMintsByAccounts(
                // Since we have no known mappings, all of them should be saved
                mapOf(
                    outcomeTransfer.to to outcomeMint,
                    outcomeTransfer.owner to outcomeMint
                )
            )
        } returns Unit

        // Events with currency token should be ignored
        coEvery { accountToMintAssociationService.isCurrencyToken(incomeMint) } returns true
        coEvery { accountToMintAssociationService.isCurrencyToken(outcomeMint) } returns false

        val event = randomLogEvent(incomeTransfer, outcomeTransfer)
        val result = filter.filter(listOf(event))

        val records = getRecords(result)
        assertThat(records).hasSize(1)
        assertThat(records[0]).isEqualTo(outcomeTransfer)
    }

    @Test
    fun `transfer with blacklisted mint`() = runBlocking<Unit> {
        val incomeTransfer = randomBalanceIncomeTransfer().copy(mint = "").copy(
            mint = blackListedToken
        )

        coEvery {
            accountToMintAssociationService.getMintsByAccounts(mutableSetOf())
        } returns mapOf()

        coEvery {
            accountToMintAssociationService.saveMintsByAccounts(mapOf())
        } returns Unit

        coEvery { accountToMintAssociationService.isCurrencyToken(blackListedToken) } returns false

        val event = randomLogEvent(incomeTransfer)
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
