package com.rarible.protocol.solana.nft.listener.service.order

import com.rarible.protocol.solana.common.event.ExecuteSaleEvent
import com.rarible.protocol.solana.common.event.OrderBuyEvent
import com.rarible.protocol.solana.common.event.OrderEvent
import com.rarible.protocol.solana.common.event.OrderSellEvent
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
                maker = record.maker,
                buyPrice = record.buyPrice,
                mint = record.mint,
                amount = record.amount,
                timestamp = record.timestamp,
                reversed = reversed,
                log = record.log
            )
        )
        is SolanaAuctionHouseOrderRecord.ExecuteSaleRecord -> {
            val direction = when (record.direction) {
                SolanaAuctionHouseOrderRecord.ExecuteSaleRecord.Direction.BUY -> ExecuteSaleEvent.Direction.BUY
                SolanaAuctionHouseOrderRecord.ExecuteSaleRecord.Direction.SELL -> ExecuteSaleEvent.Direction.SELL
            }
            listOf(
                ExecuteSaleEvent(
                    maker = record.buyer,
                    price = record.price,
                    mint = record.mint,
                    amount = record.amount,
                    timestamp = record.timestamp,
                    reversed = reversed,
                    log = record.log,
                    direction = direction
                )
            )
        }
        is SolanaAuctionHouseOrderRecord.SellRecord -> listOf(
            OrderSellEvent(
                maker = record.maker,
                sellPrice = record.sellPrice,
                mint = record.mint,
                amount = record.amount,
                timestamp = record.timestamp,
                reversed = reversed,
                log = record.log
            )
        )
    }
}
