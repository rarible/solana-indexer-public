package com.rarible.protocol.solana.nft.listener.service.order

import com.rarible.core.entity.reducer.service.Reducer
import com.rarible.protocol.solana.common.event.ExecuteSaleEvent
import com.rarible.protocol.solana.common.event.OrderBuyEvent
import com.rarible.protocol.solana.common.event.OrderEvent
import com.rarible.protocol.solana.common.event.OrderSellEvent
import com.rarible.protocol.solana.common.model.Asset
import com.rarible.protocol.solana.common.model.Order
import com.rarible.protocol.solana.common.model.OrderStatus
import com.rarible.protocol.solana.common.model.WrappedSolAssetType
import com.rarible.protocol.solana.common.records.OrderDirection
import org.springframework.stereotype.Component
import java.math.BigInteger

@Component
class ForwardOrderReducer : Reducer<OrderEvent, Order> {
    override suspend fun reduce(entity: Order, event: OrderEvent): Order {
        return when (event) {
            is ExecuteSaleEvent -> {
                if (entity == Order.empty()) {
                    // Skip reducing virtual part of the order.
                    return entity
                }
                val newFill = entity.fill + event.amount
                val isFilled = when (event.direction) {
                    OrderDirection.BUY -> newFill == entity.take.amount
                    OrderDirection.SELL -> newFill == entity.make.amount
                }
                val newStatus = if (isFilled) OrderStatus.FILLED else OrderStatus.ACTIVE
                entity.copy(
                    fill = newFill,
                    status = newStatus,
                    updatedAt = event.timestamp
                )
            }
            is OrderBuyEvent -> entity.copy(
                auctionHouse = event.auctionHouse,
                maker = event.maker,
                status = OrderStatus.ACTIVE,
                make = Asset(
                    type = WrappedSolAssetType,
                    amount = event.buyPrice
                ),
                take = event.buyAsset,
                fill = BigInteger.ZERO,
                createdAt = event.timestamp,
                updatedAt = event.timestamp,
                revertableEvents = emptyList(),
                id = Order.calculateAuctionHouseOrderId(
                    maker = event.maker,
                    mint = event.buyAsset.type.tokenAddress,
                    direction = OrderDirection.BUY,
                    auctionHouse = event.auctionHouse
                )
            )
            is OrderSellEvent -> entity.copy(
                auctionHouse = event.auctionHouse,
                maker = event.maker,
                status = OrderStatus.ACTIVE,
                make = event.sellAsset,
                take = Asset(
                    type = WrappedSolAssetType,
                    amount = event.sellPrice
                ),
                fill = BigInteger.ZERO,
                createdAt = event.timestamp,
                updatedAt = event.timestamp,
                revertableEvents = emptyList(),
                id = Order.calculateAuctionHouseOrderId(
                    maker = event.maker,
                    mint = event.sellAsset.type.tokenAddress,
                    direction = OrderDirection.SELL,
                    auctionHouse = event.auctionHouse
                )
            )
        }.copy(updatedAt = event.timestamp)
    }
}
