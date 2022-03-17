package com.rarible.protocol.solana.nft.listener.service.subscribers

import com.rarible.blockchain.scanner.framework.data.LogEvent
import com.rarible.blockchain.scanner.solana.model.SolanaDescriptor
import com.rarible.blockchain.scanner.solana.model.SolanaLogRecord
import com.rarible.blockchain.scanner.solana.subscriber.SolanaLogEventFilter
import com.rarible.protocol.solana.nft.listener.service.AccountToMintAssociationService
import com.rarible.protocol.solana.nft.listener.service.records.SolanaAuctionHouseRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaBalanceRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaBaseLogRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaMetaRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaTokenRecord
import org.springframework.stereotype.Component

@Component
class SolanaLogEventFilter(
    private val accountToMintAssociationService: AccountToMintAssociationService
) : SolanaLogEventFilter {

    override suspend fun filter(
        events: List<LogEvent<SolanaLogRecord, SolanaDescriptor>>
    ): List<LogEvent<SolanaLogRecord, SolanaDescriptor>> {
        val accountToMints = getAccountToMintMapping(events)
        return events.map { event ->
            event.copy(
                logRecordsToInsert = event.logRecordsToInsert.filter {
                    it !is SolanaBalanceRecord || !shouldIgnore(it, accountToMints)
                }
            )
        }
    }

    private suspend fun getAccountToMintMapping(
        events: List<LogEvent<SolanaLogRecord, SolanaDescriptor>>
    ): Map<String, String> {
        val accountToMintMapping = HashMap<String, String>()
        val accounts = mutableSetOf<String>()

        events.map { it.logRecordsToInsert }.flatten().forEach {
            when (it) {
                // In-memory account mapping
                is SolanaBalanceRecord.InitializeBalanceAccountRecord -> accountToMintMapping[it.balanceAccount] = it.mint
                // Accounts without known mint
                is SolanaBalanceRecord.TransferOutcomeRecord -> accounts.add(it.from)
                is SolanaBalanceRecord.TransferIncomeRecord -> accounts.add(it.to)
            }
        }

        accountToMintAssociationService.saveAccountToMintMapping(accountToMintMapping)

        // Remove known mint mapping, we don't need to query them
        accounts.removeAll(accountToMintMapping.keys)

        // Adding mapping from cache/db
        accountToMintMapping.putAll(accountToMintAssociationService.getMintsByAccounts(accounts))

        return accountToMintMapping
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
        // If we can't determine type of mint, we should not skip such events
        val mint = knownMint ?: accountToMints[account] ?: return false
        return accountToMintAssociationService.isCurrencyToken(mint)
    }

}
