package com.rarible.protocol.solana.nft.listener.service.order

import com.rarible.core.entity.reducer.service.EntityIdService
import com.rarible.protocol.solana.common.event.ExecuteSaleEvent
import com.rarible.protocol.solana.common.event.OrderBuyEvent
import com.rarible.protocol.solana.common.event.OrderCancelEvent
import com.rarible.protocol.solana.common.event.OrderEvent
import com.rarible.protocol.solana.common.event.OrderSellEvent
import com.rarible.protocol.solana.common.model.Order
import com.rarible.protocol.solana.common.model.OrderId
import com.rarible.protocol.solana.common.records.OrderDirection
import org.springframework.stereotype.Component

@Component
class OrderIdService : EntityIdService<OrderEvent, OrderId> {
    override fun getEntityId(event: OrderEvent): OrderId =
        when (event) {
            is ExecuteSaleEvent -> Order.calculateAuctionHouseOrderId(
                maker = when (event.direction) {
                    OrderDirection.BUY -> event.buyer
                    OrderDirection.SELL -> event.seller
                },
                mint = event.mint,
                direction = event.direction,
                auctionHouse = event.auctionHouse
            )
            is OrderBuyEvent -> Order.calculateAuctionHouseOrderId(
                maker = event.maker,
                mint = event.buyAsset.type.tokenAddress,
                direction = OrderDirection.BUY,
                auctionHouse = event.auctionHouse
            )
            is OrderSellEvent -> Order.calculateAuctionHouseOrderId(
                maker = event.maker,
                mint = event.sellAsset.type.tokenAddress,
                direction = OrderDirection.SELL,
                auctionHouse = event.auctionHouse
            )
            is OrderCancelEvent -> Order.calculateAuctionHouseOrderId(
                maker = event.maker,
                mint = event.mint,
                direction = event.direction,
                auctionHouse = event.auctionHouse
            )
        }
}