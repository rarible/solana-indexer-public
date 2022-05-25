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
import java.math.BigDecimal

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
    ): OrderMatchActivityDto {
        val make = Asset(TokenNftAssetType(record.mint), record.amount)
        val take = Asset(WrappedSolAssetType(), record.price)
        return OrderMatchActivityDto(
            id = record.id,
            date = record.timestamp,
            type = when (record.direction) {
                OrderDirection.SELL -> OrderMatchActivityDto.Type.SELL
                OrderDirection.BUY -> OrderMatchActivityDto.Type.ACCEPT_BID
            },
            nft = assetConverter.convert(make),
            payment = assetConverter.convert(take),
            buyer = record.buyer,
            seller = record.seller,
            price = getPrice(make, take, record.direction),
            blockchainInfo = SolanaLogToActivityBlockchainInfoConverter.convert(record.log),
            reverted = reverted,
        )
    }

    private suspend fun createListActivity(
        record: SolanaAuctionHouseOrderRecord.SellRecord,
        reverted: Boolean,
    ): OrderListActivityDto {
        val make = Asset(TokenNftAssetType(record.mint), record.amount)
        val take = Asset(WrappedSolAssetType(), record.sellPrice)
        return OrderListActivityDto(
            id = record.id,
            date = record.timestamp,
            hash = record.orderId,
            maker = record.maker,
            make = assetConverter.convert(make),
            take = assetConverter.convert(take),
            price = getPrice(make, take, OrderDirection.SELL),
            blockchainInfo = SolanaLogToActivityBlockchainInfoConverter.convert(record.log),
            reverted = reverted,
        )
    }

    private suspend fun createBidActivity(
        record: SolanaAuctionHouseOrderRecord.BuyRecord,
        reverted: Boolean
    ): OrderBidActivityDto {
        val make = Asset(WrappedSolAssetType(), record.buyPrice)
        val take = Asset(TokenNftAssetType(record.mint), record.amount)
        return OrderBidActivityDto(
            id = record.id,
            date = record.timestamp,
            hash = record.orderId,
            maker = record.maker,
            make = assetConverter.convert(make),
            take = assetConverter.convert(take),
            price = getPrice(make, take, OrderDirection.BUY),
            blockchainInfo = SolanaLogToActivityBlockchainInfoConverter.convert(record.log),
            reverted = reverted
        )
    }

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

    private suspend fun getPrice(
        make: Asset,
        take: Asset,
        orderDirection: OrderDirection
    ): BigDecimal {
        val (makePrice, takePrice) = priceNormalizer.calculateMakeAndTakePrice(make, take, orderDirection)
        return when (orderDirection) {
            OrderDirection.BUY -> takePrice!!
            OrderDirection.SELL -> makePrice!!
        }
    }
}
