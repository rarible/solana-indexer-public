package com.rarible.protocol.solana.nft.listener.service.subscribers

import com.rarible.blockchain.scanner.framework.data.LogEvent
import com.rarible.blockchain.scanner.solana.model.SolanaDescriptor
import com.rarible.blockchain.scanner.solana.model.SolanaLogRecord
import com.rarible.blockchain.scanner.solana.subscriber.SolanaLogEventFilter
import com.rarible.protocol.solana.nft.listener.service.AccountToMintAssociationService
import com.rarible.protocol.solana.nft.listener.service.records.SolanaBalanceRecord
import org.springframework.stereotype.Component

@Component
class SolanaLogEventFilter(
    private val accountToMintAssociationService: AccountToMintAssociationService
) : SolanaLogEventFilter {

    override suspend fun filter(
        events: List<LogEvent<SolanaLogRecord, SolanaDescriptor>>
    ): List<LogEvent<SolanaLogRecord, SolanaDescriptor>> {

        val accountToMints = processInitRecords(events)

        return events.map { event ->
            event.copy(
                logRecordsToInsert = event.logRecordsToInsert.filterNot { shouldFilter(it, accountToMints) }
            )
        }
    }

    private suspend fun processInitRecords(
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

        accountToMintAssociationService.saveBalanceTokens(accountToMintMapping)

        // Remove known mint mapping, we don't need to query them
        accounts.removeAll(accountToMintMapping.keys)

        // Adding mapping from cache/db
        accountToMintMapping.putAll(accountToMintAssociationService.getMintsByAccounts(accounts))

        return accountToMintMapping
    }

    private suspend fun shouldFilter(record: SolanaLogRecord, accountToMints: Map<String, String>): Boolean {
        return when (record) {
            // Skip all records related to currency mint
            is SolanaBalanceRecord.MintToRecord -> accountToMintAssociationService.isCurrencyToken(record.mint)
            is SolanaBalanceRecord.BurnRecord -> accountToMintAssociationService.isCurrencyToken(record.mint)
            is SolanaBalanceRecord.TransferOutcomeRecord -> isCurrencyAccount(record.from, accountToMints)
            is SolanaBalanceRecord.TransferIncomeRecord -> isCurrencyAccount(record.to, accountToMints)
            // Other events should stay
            else -> false
        }
    }

    private suspend fun isCurrencyAccount(account: String, accountToMints: Map<String, String>): Boolean {
        // If we can't determine type of mint, we should not skip such events
        val mint = accountToMints[account] ?: return false
        return accountToMintAssociationService.isCurrencyToken(mint)
    }

}