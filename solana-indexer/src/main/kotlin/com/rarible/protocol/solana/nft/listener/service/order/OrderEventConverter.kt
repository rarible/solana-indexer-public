package com.rarible.protocol.solana.nft.listener.service.order

import com.rarible.protocol.solana.common.event.ExecuteSaleEvent
import com.rarible.protocol.solana.common.event.OrderBuyEvent
import com.rarible.protocol.solana.common.event.OrderEvent
import com.rarible.protocol.solana.common.event.OrderSellEvent
import com.rarible.protocol.solana.common.records.SolanaAuctionHouseRecord
import com.rarible.protocol.solana.common.records.SolanaAuctionHouseRecord.*
import org.springframework.stereotype.Component

@Component
class OrderEventConverter {
    suspend fun convert(
        record: SolanaAuctionHouseRecord,
        reversed: Boolean
    ): List<OrderEvent> = when (record) {
        is BuyRecord -> listOf(
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
        is ExecuteSaleRecord -> listOf(
            ExecuteSaleEvent(
                buyPrice = record.price,
                mint = record.mint,
                amount = record.amount,
                timestamp = record.timestamp,
                reversed = reversed,
                log = record.log
            )
        )
        is SellRecord -> listOf(
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
        is CreateAuctionHouseRecord, is UpdateAuctionHouseRecord -> emptyList()
    }
}
