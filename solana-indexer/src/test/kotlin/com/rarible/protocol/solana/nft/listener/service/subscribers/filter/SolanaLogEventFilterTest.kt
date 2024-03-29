package com.rarible.protocol.solana.nft.listener.service.subscribers.filter

import com.rarible.blockchain.scanner.framework.data.LogEvent
import com.rarible.blockchain.scanner.framework.data.NewStableBlockEvent
import com.rarible.blockchain.scanner.solana.model.SolanaDescriptor
import com.rarible.blockchain.scanner.solana.model.SolanaLogRecord
import com.rarible.protocol.solana.common.configuration.SolanaIndexerProperties
import com.rarible.protocol.solana.common.filter.auctionHouse.SolanaAuctionHouseFilter
import com.rarible.protocol.solana.common.filter.token.SolanaTokenFilter
import com.rarible.protocol.solana.common.pubkey.SolanaProgramId
import com.rarible.protocol.solana.common.records.SolanaTokenRecord
import com.rarible.protocol.solana.common.records.SubscriberGroup
import com.rarible.protocol.solana.nft.listener.service.AccountToMintAssociationService
import com.rarible.protocol.solana.nft.listener.service.filter.SolanaRecordsLogEventFilter
import com.rarible.protocol.solana.nft.listener.test.data.randomSolanaBlockchainBlock
import com.rarible.protocol.solana.test.BalanceRecordDataFactory
import com.rarible.protocol.solana.test.randomMint
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SolanaLogEventFilterTest {

    private val accountToMintAssociationService: AccountToMintAssociationService = mockk()

    private val auctionHouseFilter = mockk<SolanaAuctionHouseFilter>()

    private val tokenFilter = mockk<SolanaTokenFilter>()

    private val filter = SolanaRecordsLogEventFilter(
        accountToMintAssociationService = accountToMintAssociationService,
        solanaIndexerProperties = SolanaIndexerProperties(
            kafkaReplicaSet = "kafka",
            metricRootPath = "metricRootPath",
            ipfs = SolanaIndexerProperties.IpfsProperties(
                ipfsPrivateGateway = "https://nftstorage.link",
                ipfsPublicGateway = "https://ipfs.io"
            )
        ),
        tokenFilter = tokenFilter,
    )

    @BeforeEach
    fun beforeEach() {
        clearMocks(accountToMintAssociationService)
        clearMocks(tokenFilter)
        clearMocks(auctionHouseFilter)
        coJustRun { tokenFilter.addToBlacklist(any()) }
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
        val init = BalanceRecordDataFactory.randomBalanceInitRecord()

        val transferWithInitMint =
            BalanceRecordDataFactory.randomIncomeRecord().copy(mint = "").copy(account = init.account)
        val transferWithMapping = BalanceRecordDataFactory.randomOutcomeRecord().copy(mint = "")
        val transferWithoutMapping = BalanceRecordDataFactory.randomIncomeRecord().copy(mint = "")

        val initMint = init.mint
        val mappedMint = randomMint()

        // Mapped mints should be requested for all non-currency transfers
        coEvery {
            accountToMintAssociationService.getMintsByAccounts(
                mutableSetOf(
                    transferWithMapping.to,
                    transferWithMapping.account,
                    transferWithoutMapping.account,
                    transferWithoutMapping.from
                )
            )
        } returns mapOf(transferWithMapping.account to mappedMint)

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
        coEvery { tokenFilter.isAcceptableToken(initMint) } returns false
        coEvery { tokenFilter.isAcceptableToken(mappedMint) } returns true

        val event = randomLogEvent(init, transferWithInitMint, transferWithMapping, transferWithoutMapping)
        val result = filter.filter(listOf(event))

        val records = getRecords(result)

        assertThat(records).hasSize(1)
        // Passed since type of mint is not a currency, mint for transfer updated
        assertThat(records[0]).isEqualTo(transferWithMapping.copy(mint = mappedMint))
    }

    @Test
    fun `transfer with mint`() = runBlocking<Unit> {
        val incomeMint = randomMint()
        val outcomeMint = randomMint()
        val incomeTransfer = BalanceRecordDataFactory.randomIncomeRecord().copy(mint = incomeMint)
        val outcomeTransfer = BalanceRecordDataFactory.randomOutcomeRecord().copy(mint = outcomeMint)

        // We're requesting balances in any way in order to determine - should we write new mapping in DB or not
        coEvery {
            accountToMintAssociationService.getMintsByAccounts(
                mutableSetOf(
                    outcomeTransfer.to,
                    outcomeTransfer.account
                )
            )
        } returns mapOf()

        // Mapping from transfer of NFT with known mint should be stored in cache
        coEvery {
            accountToMintAssociationService.saveMintsByAccounts(
                // Since we have no known mappings, all of them should be saved
                mapOf(
                    outcomeTransfer.to to outcomeMint,
                    outcomeTransfer.account to outcomeMint
                )
            )
        } returns Unit

        // Events with currency token should be ignored
        coEvery { tokenFilter.isAcceptableToken(incomeMint) } returns false
        coEvery { tokenFilter.isAcceptableToken(outcomeMint) } returns true

        val event = randomLogEvent(incomeTransfer, outcomeTransfer)
        val result = filter.filter(listOf(event))

        val records = getRecords(result)
        assertThat(records).hasSize(1)
        assertThat(records[0]).isEqualTo(outcomeTransfer)
    }

    @Test
    fun `transfer with blacklisted mint`() = runBlocking<Unit> {
        val blackListedToken = randomMint()
        val incomeTransfer = BalanceRecordDataFactory.randomIncomeRecord().copy(mint = "").copy(
            mint = blackListedToken
        )

        coEvery {
            accountToMintAssociationService.getMintsByAccounts(mutableSetOf())
        } returns mapOf()

        coEvery {
            accountToMintAssociationService.saveMintsByAccounts(mapOf())
        } returns Unit

        coEvery { tokenFilter.isAcceptableToken(blackListedToken) } returns false

        val event = randomLogEvent(incomeTransfer)
        val result = filter.filter(listOf(event))

        val records = getRecords(result)
        assertThat(records).hasSize(0)
    }

    private fun randomLogEvent(vararg records: SolanaLogRecord): LogEvent<SolanaLogRecord, SolanaDescriptor> {
        val blockEvent = NewStableBlockEvent(
            randomSolanaBlockchainBlock(
                // TODO: complete the test.
                logs = emptyList()
            )
        )
        return LogEvent(
            blockEvent = blockEvent,
            descriptor = object : SolanaDescriptor(
                programId = SolanaProgramId.SPL_TOKEN_PROGRAM,
                id = "test_descriptor",
                groupId = SubscriberGroup.TOKEN.id,
                entityType = SolanaTokenRecord.InitializeMintRecord::class.java,
                collection = SubscriberGroup.TOKEN.collectionName
            ) {},
            logRecordsToInsert = records.toList(),
            logRecordsToUpdate = emptyList()
        )
    }

    private fun getRecords(logEvents: List<LogEvent<SolanaLogRecord, SolanaDescriptor>>): List<SolanaLogRecord> =
        logEvents.flatMap { it.logRecordsToInsert }
}
