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
        val accountToMints = getAccountToMintMapping(events)

        val result = events.map { event ->
            if (event.logRecordsToInsert.isEmpty()) {
                return@map event
            }
            val filteredRecords = event.logRecordsToInsert.filterNot {
                it is SolanaBaseLogRecord && shouldIgnore(it, accountToMints)
            }
            logger.info(
                "Solana batch event filtering for ${event.blockEvent} of '${event.descriptor.id}': {} of {} records remain",
                filteredRecords.size,
                event.logRecordsToInsert.size
            )
            event.copy(logRecordsToInsert = filteredRecords)
        }

        return result
    }

    private suspend fun getAccountToMintMapping(
        events: List<LogEvent<SolanaLogRecord, SolanaDescriptor>>
    ): Map<String, String> {
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
                    record.mint?.let {
                        // Here we can add both accounts to mapping with same mint
                        accountToMintMapping[record.from] = record.mint
                        accountToMintMapping[record.to] = record.mint
                    }
                }
                is SolanaBalanceRecord.TransferIncomeRecord -> {
                    accounts.add(record.to)
                    record.mint?.let {
                        accountToMintMapping[record.from] = record.mint
                        accountToMintMapping[record.to] = record.mint
                    }
                }
            }
        }

        // Remove known mint mapping, we don't need to query them
        accounts.removeAll(accountToMintMapping.keys)
        val fromCache = accountToMintAssociationService.getMintsByAccounts(accounts)

        // Saving ony non-exiting mapping
        fromCache.keys.forEach { accountToMintMapping.remove(it) }
        accountToMintAssociationService.saveAccountToMintMapping(accountToMintMapping)

        // Adding mapping from cache/db
        accountToMintMapping.putAll(fromCache)

        return accountToMintMapping + fromCache
    }

    private suspend fun shouldIgnore(record: SolanaBaseLogRecord, accountToMints: Map<String, String>): Boolean {
        return when (record) {
            is SolanaBalanceRecord.MintToRecord -> accountToMintAssociationService.isCurrencyToken(record.mint)
            is SolanaBalanceRecord.BurnRecord -> accountToMintAssociationService.isCurrencyToken(record.mint)
            is SolanaBalanceRecord.TransferOutcomeRecord -> isCurrencyAccount(record.from, record.mint, accountToMints)
            is SolanaBalanceRecord.TransferIncomeRecord -> isCurrencyAccount(record.to, record.mint, accountToMints)
            is SolanaBalanceRecord.InitializeBalanceAccountRecord -> false // We want to save the account<->mint mapping.
            is SolanaTokenRecord -> accountToMintAssociationService.isCurrencyToken(record.mint)
            is SolanaAuctionHouseRecord -> false
            is SolanaMetaRecord -> false
        }
    }

    private suspend fun isCurrencyAccount(
        account: String,
        knownMint: String?,
        accountToMints: Map<String, String>
    ): Boolean {
        val mint = knownMint ?: accountToMints[account] ?: return featureFlags.skipTransfersWithUnknownMint
        return accountToMintAssociationService.isCurrencyToken(mint)
    }

}
