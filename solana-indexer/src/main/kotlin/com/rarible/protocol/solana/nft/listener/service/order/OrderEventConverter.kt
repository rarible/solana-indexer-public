package com.rarible.protocol.solana.nft.listener.service.order

import com.rarible.protocol.solana.common.event.ExecuteSaleEvent
import com.rarible.protocol.solana.common.event.InternalUpdateEvent
import com.rarible.protocol.solana.common.event.OrderBuyEvent
import com.rarible.protocol.solana.common.event.OrderCancelEvent
import com.rarible.protocol.solana.common.event.OrderEvent
import com.rarible.protocol.solana.common.event.OrderSellEvent
import com.rarible.protocol.solana.common.model.Asset
import com.rarible.protocol.solana.common.model.TokenNftAssetType
import com.rarible.protocol.solana.common.records.SolanaAuctionHouseOrderRecord
import org.springframework.stereotype.Component

@Component
class OrderEventConverter {
    suspend fun convert(
        record: SolanaAuctionHouseOrderRecord,
        reversed: Boolean
    ): List<OrderEvent> = when (record) {
        is SolanaAuctionHouseOrderRecord.BuyRecord -> listOf(
            OrderBuyEvent(
                auctionHouse = record.auctionHouse,
                maker = record.maker,
                makerAccount = record.tokenAccount,
                buyPrice = record.buyPrice,
                buyAsset = Asset(
                    type = TokenNftAssetType(tokenAddress = record.mint),
                    amount = record.amount
                ),
                timestamp = record.timestamp,
                reversed = reversed,
                log = record.log
            )
        )
        is SolanaAuctionHouseOrderRecord.ExecuteSaleRecord -> listOf(
            ExecuteSaleEvent(
                auctionHouse = record.auctionHouse,
                buyer = record.buyer,
                seller = record.seller,
                price = record.price,
                mint = record.mint,
                amount = record.amount,
                timestamp = record.timestamp,
                reversed = reversed,
                log = record.log,
                direction = record.direction
            )
        )
        is SolanaAuctionHouseOrderRecord.SellRecord -> listOf(
            OrderSellEvent(
                auctionHouse = record.auctionHouse,
                maker = record.maker,
                makerAccount = record.tokenAccount,
                sellAsset = Asset(
                    type = TokenNftAssetType(tokenAddress = record.mint),
                    amount = record.amount
                ),
                sellPrice = record.sellPrice,
                timestamp = record.timestamp,
                reversed = reversed,
                log = record.log
            )
        )
        is SolanaAuctionHouseOrderRecord.CancelRecord -> listOf(
            OrderCancelEvent(
                maker = record.maker,
                mint = record.mint,
                price = record.price,
                amount = record.amount,
                direction = record.direction,
                log = record.log,
                timestamp = record.timestamp,
                auctionHouse = record.auctionHouse,
                reversed = reversed
            )
        )
        is SolanaAuctionHouseOrderRecord.InternalOrderUpdateRecord -> listOf(
            InternalUpdateEvent(
                auctionHouse = record.auctionHouse,
                timestamp = record.timestamp,
                reversed = false,
                log = record.log,
                orderId = record.orderId,
                instruction = record.instruction
            )
        )
        else -> emptyList()
    }
}
