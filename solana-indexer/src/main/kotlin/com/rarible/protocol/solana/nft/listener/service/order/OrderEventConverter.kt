package com.rarible.protocol.solana.nft.listener.service.order

import com.rarible.protocol.solana.common.event.ExecuteSaleEvent
import com.rarible.protocol.solana.common.event.OrderBuyEvent
import com.rarible.protocol.solana.common.event.OrderEvent
import com.rarible.protocol.solana.common.event.OrderSellEvent
import com.rarible.protocol.solana.common.model.Asset
import com.rarible.protocol.solana.common.model.TokenNftAssetType
import com.rarible.protocol.solana.common.records.*
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
                direction = when (record.direction) {
                    SolanaAuctionHouseOrderRecord.ExecuteSaleRecord.Direction.BUY -> ExecuteSaleEvent.Direction.BUY
                    SolanaAuctionHouseOrderRecord.ExecuteSaleRecord.Direction.SELL -> ExecuteSaleEvent.Direction.SELL
                }
            )
        )
        is SolanaAuctionHouseOrderRecord.SellRecord -> listOf(
            OrderSellEvent(
                auctionHouse = record.auctionHouse,
                maker = record.maker,
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
        is SolanaAuctionHouseOrderRecord.CancelRecord -> emptyList() // TODO[orders]: handle.
    }
}
