package com.rarible.protocol.solana.nft.listener.service.subscribers

import com.rarible.blockchain.scanner.framework.data.LogEvent
import com.rarible.blockchain.scanner.solana.model.SolanaDescriptor
import com.rarible.blockchain.scanner.solana.model.SolanaLogRecord
import com.rarible.blockchain.scanner.solana.subscriber.SolanaLogEventFilter
import com.rarible.protocol.solana.common.configuration.FeatureFlags
import com.rarible.protocol.solana.common.records.SolanaAuctionHouseRecord
import com.rarible.protocol.solana.common.records.SolanaBalanceRecord
import com.rarible.protocol.solana.common.records.SolanaBaseLogRecord
import com.rarible.protocol.solana.common.records.SolanaMetaRecord
import com.rarible.protocol.solana.common.records.SolanaTokenRecord
import com.rarible.protocol.solana.nft.listener.service.AccountToMintAssociationService
import com.rarible.protocol.solana.nft.listener.util.AccountGraph
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

        val accountGroups = getAccountToMintMapping(events)

        // Retrieving mapping for ALL found non-currency accounts to know what we really need to update in DB
        // TODO here we can query only accounts without known mint if we sure all mapping from init records already in DB
        // TODO If we are not starting to index from the 1st block, we MUST query mints for all accounts here
        val withoutCurrency = filterCurrencyBalances(accountGroups)
        val existMapping = accountToMintAssociationService.getMintsByAccounts(withoutCurrency)

        // Set mint for groups - since reference of AccountGroup is the same for all mapped accounts,
        // it will be automatically referenced for each account of the group
        existMapping.forEach { accountGroups[it.key]!!.mint = it.value }

        // Now we have full mapping account->mint from events and DB
        val accountToMintMapping = HashMap<String, String>()
        accountGroups.forEach { group ->
            val account = group.key
            group.value.mint?.let { accountToMintMapping[account] = it }
        }

        return coroutineScope {
            // Saving new non-currency mappings in background while filtering event list
            val mappingToSave = HashMap(accountToMintMapping.filter {
                !accountToMintAssociationService.isCurrencyToken(it.value)
            })
            val updateMappingDeferred = async {
                // Saving only non-existing mapping, including account references
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

    private fun filter(
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

    private fun getAccountToMintMapping(
        events: List<LogEvent<SolanaLogRecord, SolanaDescriptor>>
    ): Map<String, AccountGraph.AccountGroup> {
        val accountToMintMapping = HashMap<String, String>()
        val accounts = AccountGraph()

        events.asSequence().flatMap { it.logRecordsToInsert }.forEach { record ->
            when (record) {
                // In-memory account mapping
                is SolanaBalanceRecord.InitializeBalanceAccountRecord -> {
                    // Artificial reference for init-record
                    accounts.addRib(record.balanceAccount, record.balanceAccount)
                    accountToMintMapping[record.balanceAccount] = record.mint
                }
                is SolanaBalanceRecord.TransferOutcomeRecord -> {
                    accounts.addRib(record.from, record.to)
                    record.mint?.let { accountToMintMapping[record.from] = it }
                }
                is SolanaBalanceRecord.TransferIncomeRecord -> {
                    accounts.addRib(record.from, record.to)
                    record.mint?.let { accountToMintMapping[record.to] = it }
                }
            }
        }

        val accountGroups = accounts.findGroups()

        // Set mints known from log events
        accountToMintMapping.forEach {
            accountGroups[it.key]!!.mint = it.value
        }

        return accountGroups
    }

    private fun keepIfNft(
        record: SolanaBaseLogRecord,
        accountToMintMapping: Map<String, String>
    ): SolanaBaseLogRecord? = when (record) {
        is SolanaBalanceRecord.MintToRecord -> keepIfNft(record, record.mint)
        is SolanaBalanceRecord.BurnRecord -> keepIfNft(record, record.mint)
        is SolanaBalanceRecord.TransferOutcomeRecord -> {
            keepIfNft(record.from, record.mint, accountToMintMapping, record) { record.copy(mint = it) }
        }
        is SolanaBalanceRecord.TransferIncomeRecord -> {
            keepIfNft(record.to, record.mint, accountToMintMapping, record) { record.copy(mint = it) }
        }
        is SolanaBalanceRecord.InitializeBalanceAccountRecord -> keepIfNft(record, record.mint)
        is SolanaTokenRecord -> keepIfNft(record, record.mint)
        is SolanaAuctionHouseRecord -> record
        is SolanaMetaRecord -> record
    }

    private fun keepIfNft(record: SolanaBaseLogRecord, mint: String): SolanaBaseLogRecord? {
        return if (accountToMintAssociationService.isCurrencyToken(mint)) {
            null
        } else {
            record
        }
    }

    private fun <T> keepIfNft(
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

    private fun filterCurrencyBalances(groups: Map<String, AccountGraph.AccountGroup>): Set<String> {
        return groups.mapNotNull {
            val account = it.key
            val mint = it.value.mint
            if (mint == null || !accountToMintAssociationService.isCurrencyToken(mint)) {
                account
            } else {
                null
            }
        }.toMutableSet()
    }
}
