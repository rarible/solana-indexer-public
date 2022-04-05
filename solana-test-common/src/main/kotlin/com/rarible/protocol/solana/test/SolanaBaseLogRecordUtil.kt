package com.rarible.protocol.solana.test

import com.rarible.blockchain.scanner.solana.model.SolanaLog
import com.rarible.protocol.solana.common.records.SolanaAuctionHouseOrderRecord
import com.rarible.protocol.solana.common.records.SolanaAuctionHouseRecord
import com.rarible.protocol.solana.common.records.SolanaBalanceRecord
import com.rarible.protocol.solana.common.records.SolanaBaseLogRecord
import com.rarible.protocol.solana.common.records.SolanaMetaRecord
import com.rarible.protocol.solana.common.records.SolanaTokenRecord

@Suppress("UNCHECKED_CAST")
fun <T : SolanaBaseLogRecord> T.withUpdatedLog(
    log: SolanaLog
): T = when (val r = this as SolanaBaseLogRecord) {
    is SolanaAuctionHouseOrderRecord.BuyRecord -> r.copy(log = log) as T
    is SolanaAuctionHouseOrderRecord.CancelRecord -> r.copy(log = log) as T
    is SolanaAuctionHouseOrderRecord.ExecuteSaleRecord -> r.copy(log = log) as T
    is SolanaAuctionHouseOrderRecord.SellRecord -> r.copy(log = log) as T
    is SolanaAuctionHouseOrderRecord.InternalOrderUpdateRecord -> r.copy(log = log) as T
    is SolanaAuctionHouseRecord.CreateAuctionHouseRecord -> r.copy(log = log) as T
    is SolanaAuctionHouseRecord.UpdateAuctionHouseRecord -> r.copy(log = log) as T
    is SolanaBalanceRecord.BurnRecord -> r.copy(log = log) as T
    is SolanaBalanceRecord.InitializeBalanceAccountRecord -> r.copy(log = log) as T
    is SolanaBalanceRecord.MintToRecord -> r.copy(log = log) as T
    is SolanaBalanceRecord.TransferIncomeRecord -> r.copy(log = log) as T
    is SolanaBalanceRecord.TransferOutcomeRecord -> r.copy(log = log) as T
    is SolanaMetaRecord.MetaplexCreateMetadataAccountRecord -> r.copy(log = log) as T
    is SolanaMetaRecord.MetaplexSignMetadataRecord -> r.copy(log = log) as T
    is SolanaMetaRecord.MetaplexUnVerifyCollectionRecord -> r.copy(log = log) as T
    is SolanaMetaRecord.MetaplexUpdateMetadataRecord -> r.copy(log = log) as T
    is SolanaMetaRecord.MetaplexVerifyCollectionRecord -> r.copy(log = log) as T
    is SolanaMetaRecord.SetAndVerifyMetadataRecord -> r.copy(log = log) as T
    is SolanaTokenRecord.BurnRecord -> r.copy(log = log) as T
    is SolanaTokenRecord.InitializeMintRecord -> r.copy(log = log) as T
    is SolanaTokenRecord.MintToRecord -> r.copy(log = log) as T
}