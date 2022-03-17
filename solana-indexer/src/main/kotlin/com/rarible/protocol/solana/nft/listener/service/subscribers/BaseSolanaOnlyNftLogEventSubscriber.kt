package com.rarible.protocol.solana.nft.listener.service.subscribers

import com.rarible.blockchain.scanner.solana.client.SolanaBlockchainBlock
import com.rarible.blockchain.scanner.solana.client.SolanaBlockchainLog
import com.rarible.blockchain.scanner.solana.subscriber.SolanaLogEventSubscriber
import com.rarible.protocol.solana.nft.listener.service.AccountToMintAssociationService
import com.rarible.protocol.solana.nft.listener.service.records.SolanaAuctionHouseRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaBalanceRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaBaseLogRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaMetaRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaTokenRecord

/**
 * Base subscriber of Solana events that ignores non-NFT events.
 *
 * Superclass for token and balance subscribers.
 * Meta and auction house events don't need to be ignored.
 */
abstract class BaseSolanaOnlyNftLogEventSubscriber(
    private val accountToMintAssociationService: AccountToMintAssociationService
) : SolanaLogEventSubscriber {

    abstract suspend fun parseSolanaLogRecords(
        block: SolanaBlockchainBlock,
        log: SolanaBlockchainLog
    ): List<SolanaBaseLogRecord>

    final override suspend fun getEventRecords(
        block: SolanaBlockchainBlock,
        log: SolanaBlockchainLog
    ): List<SolanaBaseLogRecord> =
        parseSolanaLogRecords(block, log).filterNot { shouldIgnoreRecord(it) }

    private suspend fun shouldIgnoreRecord(record: SolanaBaseLogRecord): Boolean {
        val mint = when (record) {
            is SolanaTokenRecord.BurnRecord -> record.mint
            is SolanaTokenRecord.InitializeMintRecord -> record.mint
            is SolanaTokenRecord.MintToRecord -> record.mint

            is SolanaBalanceRecord.BurnRecord -> record.mint
            is SolanaBalanceRecord.InitializeBalanceAccountRecord -> record.mint
            is SolanaBalanceRecord.MintToRecord -> record.mint
            is SolanaBalanceRecord.TransferIncomeRecord -> accountToMintAssociationService.getMintByAccount(record.account)
                ?: return true // We must have seen the 'initialize account record'.
            is SolanaBalanceRecord.TransferOutcomeRecord -> accountToMintAssociationService.getMintByAccount(record.account)
                ?: return true // We must have seen the 'initialize account record'.

            // Auction house events - never ignore.
            is SolanaAuctionHouseRecord-> return false

            // Metaplex meta - never ignore.
            is SolanaMetaRecord -> return false
        }
        return accountToMintAssociationService.isCurrencyToken(mint)
    }
}
