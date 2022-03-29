package com.rarible.protocol.solana.nft.listener.service.order

import com.rarible.core.entity.reducer.service.EntityIdService
import com.rarible.protocol.solana.common.event.ExecuteSaleEvent
import com.rarible.protocol.solana.common.event.OrderBuyEvent
import com.rarible.protocol.solana.common.event.OrderEvent
import com.rarible.protocol.solana.common.event.OrderSellEvent
import com.rarible.protocol.solana.common.model.Order
import com.rarible.protocol.solana.common.model.OrderId
import com.rarible.protocol.solana.common.model.TokenNftAssetType
import com.rarible.protocol.solana.common.model.WrappedSolAssetType
import org.springframework.stereotype.Component

@Component
class OrderIdService : EntityIdService<OrderEvent, OrderId> {
    override fun getEntityId(event: OrderEvent): OrderId =
        when (event) {
            is ExecuteSaleEvent -> Order.calculateAuctionHouseOrderId(
                maker = when (event.direction) {
                    Direction.BUY -> event.buyer
                    Direction.SELL -> event.seller
                },
                make = when (event.direction) {
                    Direction.BUY -> WrappedSolAssetType
                    Direction.SELL -> TokenNftAssetType(event.mint)
                },
                auctionHouse = event.auctionHouse
            )
            is OrderBuyEvent -> Order.calculateAuctionHouseOrderId(
                maker = event.maker,
                make = WrappedSolAssetType,
                auctionHouse = event.auctionHouse
            )
            is OrderSellEvent -> Order.calculateAuctionHouseOrderId(
                maker = event.maker,
                make = event.sellAsset.type,
                auctionHouse = event.auctionHouse
            )
        }
}