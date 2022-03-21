package com.rarible.protocol.solana.nft.listener.service.order

import com.rarible.core.entity.reducer.service.Reducer
import com.rarible.protocol.solana.common.event.ExecuteSaleEvent
import com.rarible.protocol.solana.common.event.OrderBuyEvent
import com.rarible.protocol.solana.common.event.OrderEvent
import com.rarible.protocol.solana.common.event.OrderSellEvent
import com.rarible.protocol.solana.common.model.Asset
import com.rarible.protocol.solana.common.model.Order
import com.rarible.protocol.solana.common.model.OrderStatus
import com.rarible.protocol.solana.common.model.OrderType
import com.rarible.protocol.solana.common.model.TokenAssetType
import org.springframework.stereotype.Component
import java.util.*

@Component
class ForwardOrderReducer : Reducer<OrderEvent, Order> {
    override suspend fun reduce(entity: Order, event: OrderEvent): Order {
        return when (event) {
            is ExecuteSaleEvent -> entity.copy(
                status = OrderStatus.ENDED
            )
            is OrderBuyEvent -> entity.copy(
                maker = event.maker,
                status = OrderStatus.ACTIVE,
                type = OrderType.BUY,
                salt = UUID.randomUUID().toString(), // TODO is it ok ?
                cancelled = false,
                make = Asset(
                    type = TokenAssetType(tokenAddress = event.mint),
                    amount = event.amount
                ),
                createdAt = event.timestamp
            )
            is OrderSellEvent -> entity.copy(
                maker = event.maker,
                status = OrderStatus.ACTIVE,
                type = OrderType.SELL,
                salt = UUID.randomUUID().toString(), // TODO is it ok ?
                cancelled = false,
                make = Asset(
                    type = TokenAssetType(tokenAddress = event.mint),
                    amount = event.amount
                ),
                createdAt = event.timestamp
            )
        }.copy(updatedAt = event.timestamp)
    }
}
