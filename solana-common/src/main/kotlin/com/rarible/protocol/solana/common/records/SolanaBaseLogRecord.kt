package com.rarible.protocol.solana.common.records

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.rarible.blockchain.scanner.solana.model.SolanaLogRecord
import java.time.Instant

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes(
    JsonSubTypes.Type(name = "AUCTION_HOUSE_ORDER_BUY", value = SolanaAuctionHouseOrderRecord.BuyRecord::class),
    JsonSubTypes.Type(name = "AUCTION_HOUSE_ORDER_CANCEL", value = SolanaAuctionHouseOrderRecord.CancelRecord::class),
    JsonSubTypes.Type(name = "AUCTION_HOUSE_ORDER_EXECUTE_SALE", value = SolanaAuctionHouseOrderRecord.ExecuteSaleRecord::class),
    JsonSubTypes.Type(name = "AUCTION_HOUSE_ORDER_SELL", value = SolanaAuctionHouseOrderRecord.SellRecord::class),
    JsonSubTypes.Type(name = "AUCTION_HOUSE_CREATE", value = SolanaAuctionHouseRecord.CreateAuctionHouseRecord::class),
    JsonSubTypes.Type(name = "AUCTION_HOUSE_UPDATE", value = SolanaAuctionHouseRecord.UpdateAuctionHouseRecord::class),
    JsonSubTypes.Type(name = "BALANCE_BURN", value = SolanaBalanceRecord.BurnRecord::class),
    JsonSubTypes.Type(name = "BALANCE_INITIALIZE_ACCOUNT", value = SolanaBalanceRecord.InitializeBalanceAccountRecord::class),
    JsonSubTypes.Type(name = "BALANCE_MINT_TO", value = SolanaBalanceRecord.MintToRecord::class),
    JsonSubTypes.Type(name = "BALANCE_TRANSFER_INCOME", value = SolanaBalanceRecord.TransferIncomeRecord::class),
    JsonSubTypes.Type(name = "BALANCE_TRANSFER_OUTCOME", value = SolanaBalanceRecord.TransferOutcomeRecord::class),
    JsonSubTypes.Type(name = "METAPLEX_META_CREATE_ACCOUNT", value = SolanaMetaRecord.MetaplexCreateMetadataAccountRecord::class),
    JsonSubTypes.Type(name = "METAPLEX_META_SIGN_META", value = SolanaMetaRecord.MetaplexSignMetadataRecord::class),
    JsonSubTypes.Type(name = "METAPLEX_META_UN_VERIFY_COLLECTION", value = SolanaMetaRecord.MetaplexUnVerifyCollectionRecord::class),
    JsonSubTypes.Type(name = "METAPLEX_META_UPDATE", value = SolanaMetaRecord.MetaplexUpdateMetadataRecord::class),
    JsonSubTypes.Type(name = "METAPLEX_META_VERIFY_COLLECTION", value = SolanaMetaRecord.MetaplexVerifyCollectionRecord::class),
    JsonSubTypes.Type(name = "METAPLEX_META_SET_AND_VERIFY", value = SolanaMetaRecord.SetAndVerifyMetadataRecord::class),
    JsonSubTypes.Type(name = "TOKEN_BURN", value = SolanaTokenRecord.BurnRecord::class),
    JsonSubTypes.Type(name = "TOKEN_INITIALIZE_MINT", value = SolanaTokenRecord.InitializeMintRecord::class),
    JsonSubTypes.Type(name = "TOKEN_MINT_TOK", value = SolanaTokenRecord.MintToRecord::class)
)
sealed class SolanaBaseLogRecord : SolanaLogRecord() {
    abstract val timestamp: Instant
}

/**
 * Do not forget to list a [JsonSubTypes.Type] above for the newly registered class.
 */
@Suppress("unused")
private fun compilationChecker(record: SolanaBaseLogRecord): Unit = when(record) {
    is SolanaAuctionHouseOrderRecord.BuyRecord -> TODO()
    is SolanaAuctionHouseOrderRecord.CancelRecord -> TODO()
    is SolanaAuctionHouseOrderRecord.ExecuteSaleRecord -> TODO()
    is SolanaAuctionHouseOrderRecord.SellRecord -> TODO()
    is SolanaAuctionHouseRecord.CreateAuctionHouseRecord -> TODO()
    is SolanaAuctionHouseRecord.UpdateAuctionHouseRecord -> TODO()
    is SolanaBalanceRecord.BurnRecord -> TODO()
    is SolanaBalanceRecord.InitializeBalanceAccountRecord -> TODO()
    is SolanaBalanceRecord.MintToRecord -> TODO()
    is SolanaBalanceRecord.TransferIncomeRecord -> TODO()
    is SolanaBalanceRecord.TransferOutcomeRecord -> TODO()
    is SolanaMetaRecord.MetaplexCreateMetadataAccountRecord -> TODO()
    is SolanaMetaRecord.MetaplexSignMetadataRecord -> TODO()
    is SolanaMetaRecord.MetaplexUnVerifyCollectionRecord -> TODO()
    is SolanaMetaRecord.MetaplexUpdateMetadataRecord -> TODO()
    is SolanaMetaRecord.MetaplexVerifyCollectionRecord -> TODO()
    is SolanaMetaRecord.SetAndVerifyMetadataRecord -> TODO()
    is SolanaTokenRecord.BurnRecord -> TODO()
    is SolanaTokenRecord.InitializeMintRecord -> TODO()
    is SolanaTokenRecord.MintToRecord -> TODO()
}
