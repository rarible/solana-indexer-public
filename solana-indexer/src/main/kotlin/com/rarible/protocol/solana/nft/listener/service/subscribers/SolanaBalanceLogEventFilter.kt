package com.rarible.protocol.solana.nft.listener.service.subscribers

import com.rarible.blockchain.scanner.framework.data.LogEvent
import com.rarible.blockchain.scanner.solana.model.SolanaDescriptor
import com.rarible.blockchain.scanner.solana.model.SolanaLogRecord
import com.rarible.blockchain.scanner.solana.subscriber.SolanaLogEventFilter
import com.rarible.protocol.solana.common.configuration.FeatureFlags
import com.rarible.protocol.solana.nft.listener.service.AccountToMintAssociationService
import com.rarible.protocol.solana.nft.listener.service.records.SolanaAuctionHouseRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaBalanceRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaBaseLogRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaMetaRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaTokenRecord
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component

class SolanaBalanceLogEventFilter(
    private val accountToMintAssociationService: AccountToMintAssociationService,
    private val featureFlags: FeatureFlags
) : SolanaLogEventFilter {

    private val logger = LoggerFactory.getLogger(javaClass)

    override suspend fun filter(
        events: List<LogEvent<SolanaLogRecord, SolanaDescriptor>>
    ): List<LogEvent<SolanaLogRecord, SolanaDescriptor>> {
        if (events.isEmpty()) {
            return emptyList()
        }

        val mappingsFromEvents = getAccountToMintMapping(events)
        val accountToMintMapping = mappingsFromEvents.accountToMintMapping

        // Retrieving mapping for ALL found accounts to know what we really need to update in DB
        val existMapping = accountToMintAssociationService.getMintsByAccounts(mappingsFromEvents.accounts)

        return coroutineScope {
            // Saving new mappings in background while filtering event list
            val mappingToSave = HashMap(accountToMintMapping)
            val updateMappingDeferred = async {
                // Saving only non-exiting mapping
                existMapping.keys.forEach { mappingToSave.remove(it) }
                accountToMintAssociationService.saveMintsByAccounts(mappingToSave)
            }

            // Adding mapping from cache/db
            accountToMintMapping.putAll(existMapping)
            val result = filter(events, accountToMintMapping)

            updateMappingDeferred.await()

            result
        }
    }

    private suspend fun filter(
        events: List<LogEvent<SolanaLogRecord, SolanaDescriptor>>,
        accountToMints: Map<String, String>
    ): List<LogEvent<SolanaLogRecord, SolanaDescriptor>> {
        return events.map { event ->
            if (event.logRecordsToInsert.isEmpty()) {
                return@map event
            }
            val filteredRecords = event.logRecordsToInsert.mapNotNull {
                if (it is SolanaBaseLogRecord) {
                    // Keep only NFT logs
                    keepIfNft(it, accountToMints)
                } else {
                    // Keep non-target logs
                    it
                }
            }
            logger.info(
                "Solana batch event filtering for ${event.blockEvent} of '${event.descriptor.id}': {} of {} records remain",
                filteredRecords.size,
                event.logRecordsToInsert.size
            )
            event.copy(logRecordsToInsert = filteredRecords)
        }
    }

    private suspend fun getAccountToMintMapping(
        events: List<LogEvent<SolanaLogRecord, SolanaDescriptor>>
    ): MappingsFromLogEvents {
        val accountToMintMapping = HashMap<String, String>()
        val accounts = mutableSetOf<String>()

        events.asSequence().flatMap { it.logRecordsToInsert }.forEach { record ->
            when (record) {
                // In-memory account mapping
                is SolanaBalanceRecord.InitializeBalanceAccountRecord -> {
                    accountToMintMapping[record.balanceAccount] = record.mint
                }
                is SolanaBalanceRecord.TransferOutcomeRecord -> {
                    accounts.add(record.from)
                    record.mint?.let { accountToMintMapping[record.from] = record.mint }
                }
                is SolanaBalanceRecord.TransferIncomeRecord -> {
                    accounts.add(record.to)
                    record.mint?.let { accountToMintMapping[record.to] = record.mint }
                }
            }
        }
        return MappingsFromLogEvents(accounts, accountToMintMapping)

    }

    private suspend fun keepIfNft(
        record: SolanaBaseLogRecord, accountToMints: Map<String, String>
    ): SolanaBaseLogRecord? {
        return when (record) {
            is SolanaBalanceRecord.MintToRecord -> keepIfNft(record, record.mint)
            is SolanaBalanceRecord.BurnRecord -> keepIfNft(record, record.mint)
            is SolanaBalanceRecord.TransferOutcomeRecord -> {
                keepIfNft(record.from, record.mint, accountToMints, record) { record.copy(mint = it) }
            }
            is SolanaBalanceRecord.TransferIncomeRecord -> {
                keepIfNft(record.to, record.mint, accountToMints, record) { record.copy(mint = it) }
            }
            is SolanaBalanceRecord.InitializeBalanceAccountRecord -> null // Skipping init records
            is SolanaTokenRecord -> keepIfNft(record, record.mint)
            is SolanaAuctionHouseRecord -> record
            is SolanaMetaRecord -> record
        }
    }

    private fun keepIfNft(record: SolanaBaseLogRecord, mint: String): SolanaBaseLogRecord? {
        return if (accountToMintAssociationService.isCurrencyToken(mint)) {
            null
        } else {
            record
        }
    }

    private suspend fun <T> keepIfNft(
        account: String,
        knownMint: String?,
        accountToMints: Map<String, String>,
        record: T,
        updateMint: (String) -> T
    ): T? {

        val mint = knownMint ?: accountToMints[account]
        // If mint not found, we can skip record - depends on feature flag
        ?: return if (featureFlags.skipTransfersWithUnknownMint) null else record

        if (accountToMintAssociationService.isCurrencyToken(mint)) {
            return null
        }

        // Update mint field in record if it is not specified,
        // otherwise return record 'as is' just to save some CPU time
        return if (knownMint == null) {
            updateMint(mint)
        } else {
            record
        }
    }

    data class MappingsFromLogEvents(
        val accounts: Set<String>,
        val accountToMintMapping: MutableMap<String, String>
    )

}
