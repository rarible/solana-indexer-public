package com.rarible.protocol.solana.common.converter

import com.rarible.protocol.solana.common.model.Asset
import com.rarible.protocol.solana.common.model.TokenNftAssetType
import com.rarible.protocol.solana.common.model.WrappedSolAssetType
import com.rarible.protocol.solana.common.records.OrderDirection
import com.rarible.protocol.solana.common.records.SolanaAuctionHouseOrderRecord
import com.rarible.protocol.solana.common.service.PriceNormalizer
import com.rarible.protocol.solana.dto.ActivityDto
import com.rarible.protocol.solana.dto.OrderBidActivityDto
import com.rarible.protocol.solana.dto.OrderCancelBidActivityDto
import com.rarible.protocol.solana.dto.OrderCancelListActivityDto
import com.rarible.protocol.solana.dto.OrderListActivityDto
import com.rarible.protocol.solana.dto.OrderMatchActivityDto
import com.rarible.protocol.solana.dto.SolanaNftAssetTypeDto
import com.rarible.protocol.solana.dto.SolanaSolAssetTypeDto
import org.springframework.stereotype.Component

@Component
class SolanaAuctionHouseOrderActivityConverter(
    private val assetConverter: AssetConverter,
    private val priceNormalizer: PriceNormalizer,
) {

    suspend fun convert(
        record: SolanaAuctionHouseOrderRecord,
        reverted: Boolean,
    ): ActivityDto? = when (record) {
        is SolanaAuctionHouseOrderRecord.SellRecord -> createListActivity(record, reverted)
        is SolanaAuctionHouseOrderRecord.BuyRecord -> createBidActivity(record, reverted)
        is SolanaAuctionHouseOrderRecord.CancelRecord -> createCancelActivity(record, reverted)
        is SolanaAuctionHouseOrderRecord.ExecuteSaleRecord -> createMatchActivity(record, reverted)
        // Should be never found in DB
        is SolanaAuctionHouseOrderRecord.InternalOrderUpdateRecord -> null
    }

    private suspend fun createMatchActivity(
        record: SolanaAuctionHouseOrderRecord.ExecuteSaleRecord,
        reverted: Boolean,
    ) = OrderMatchActivityDto(
        id = record.id,
        date = record.timestamp,
        type = when (record.direction) {
            OrderDirection.SELL -> OrderMatchActivityDto.Type.SELL
            OrderDirection.BUY -> OrderMatchActivityDto.Type.ACCEPT_BID
        },
        nft = assetConverter.convert(Asset(TokenNftAssetType(record.mint), record.amount)),
        payment = assetConverter.convert(Asset(WrappedSolAssetType(), record.price)),
        buyer = record.buyer,
        seller = record.seller,
        price = priceNormalizer.normalize(Asset(WrappedSolAssetType(), record.price)),
        blockchainInfo = SolanaLogToActivityBlockchainInfoConverter.convert(record.log),
        reverted = reverted,
    )

    private suspend fun createListActivity(
        record: SolanaAuctionHouseOrderRecord.SellRecord,
        reverted: Boolean,
    ) = OrderListActivityDto(
        id = record.id,
        date = record.timestamp,
        hash = record.orderId,
        maker = record.maker,
        make = assetConverter.convert(Asset(TokenNftAssetType(record.mint), record.amount)),
        take = assetConverter.convert(Asset(WrappedSolAssetType(), record.sellPrice)),
        price = priceNormalizer.normalize(WrappedSolAssetType(), record.sellPrice),
        blockchainInfo = SolanaLogToActivityBlockchainInfoConverter.convert(record.log),
        reverted = reverted,
    )

    private suspend fun createBidActivity(
        record: SolanaAuctionHouseOrderRecord.BuyRecord,
        reverted: Boolean,
    ) = OrderBidActivityDto(
        id = record.id,
        date = record.timestamp,
        hash = record.orderId,
        maker = record.maker,
        make = assetConverter.convert(Asset(WrappedSolAssetType(), record.buyPrice)),
        take = assetConverter.convert(Asset(TokenNftAssetType(record.mint), record.amount)),
        price = priceNormalizer.normalize(WrappedSolAssetType(), record.buyPrice),
        blockchainInfo = SolanaLogToActivityBlockchainInfoConverter.convert(record.log),
        reverted = reverted,
    )

    private fun createCancelActivity(
        record: SolanaAuctionHouseOrderRecord.CancelRecord,
        reverted: Boolean,
    ) = when (record.direction) {
        OrderDirection.SELL -> OrderCancelListActivityDto(
            id = record.id,
            date = record.timestamp,
            hash = record.orderId,
            maker = record.maker,
            make = SolanaNftAssetTypeDto(record.mint),
            take = SolanaSolAssetTypeDto(),
            blockchainInfo = SolanaLogToActivityBlockchainInfoConverter.convert(record.log),
            reverted = reverted,
        )
        OrderDirection.BUY -> OrderCancelBidActivityDto(
            id = record.id,
            date = record.timestamp,
            hash = record.orderId,
            maker = record.maker,
            make = SolanaSolAssetTypeDto(),
            take = SolanaNftAssetTypeDto(record.mint),
            blockchainInfo = SolanaLogToActivityBlockchainInfoConverter.convert(record.log),
            reverted = reverted,
        )
    }
}
