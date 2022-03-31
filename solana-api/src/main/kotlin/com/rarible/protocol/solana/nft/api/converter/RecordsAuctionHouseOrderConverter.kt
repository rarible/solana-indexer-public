package com.rarible.protocol.solana.nft.api.converter

import com.rarible.protocol.solana.common.records.SolanaAuctionHouseOrderRecord
import com.rarible.protocol.solana.dto.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull

object RecordsAuctionHouseOrderConverter : ActivityConverter<SolanaAuctionHouseOrderRecord> {
    override fun convert(flow: Flow<SolanaAuctionHouseOrderRecord>): Flow<ActivityDto> =
        flow.mapNotNull(RecordsAuctionHouseOrderConverter::convert)

    private fun convert(record: SolanaAuctionHouseOrderRecord) = when (record) {
        is SolanaAuctionHouseOrderRecord.BuyRecord -> OrderListActivityDto(
            id = record.id,
            date = record.timestamp,
            hash = record.orderId,
            maker = record.maker,
            make = AssetDto(SolanaNftAssetTypeDto(record.mint), record.amount.toBigDecimal()),
            take = AssetDto(SolanaSolAssetTypeDto(), record.buyPrice.toBigDecimal()),
            price = record.buyPrice.toBigDecimal(),
            blockchainInfo = blockchainInfo(record.log),
            reverted = false,
        )
        is SolanaAuctionHouseOrderRecord.CancelRecord -> OrderCancelListActivityDto(
            id = record.id,
            date = record.timestamp,
            hash = record.orderId,
            maker = record.maker,
            make = SolanaNftAssetTypeDto(record.mint),
            take = SolanaSolAssetTypeDto(),
            blockchainInfo = blockchainInfo(record.log),
            reverted = false,
        )
        is SolanaAuctionHouseOrderRecord.SellRecord -> OrderListActivityDto(
            id = record.id,
            date = record.timestamp,
            hash = record.orderId,
            maker = record.maker,
            make = AssetDto(SolanaNftAssetTypeDto(record.mint), record.amount.toBigDecimal()),
            take = AssetDto(SolanaSolAssetTypeDto(), record.sellPrice.toBigDecimal()),
            price = record.sellPrice.toBigDecimal(),
            blockchainInfo = blockchainInfo(record.log),
            reverted = false,
        )
        is SolanaAuctionHouseOrderRecord.ExecuteSaleRecord -> null
    }
}
