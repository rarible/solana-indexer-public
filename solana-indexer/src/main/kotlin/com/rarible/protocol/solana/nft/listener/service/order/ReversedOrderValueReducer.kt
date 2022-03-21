package com.rarible.protocol.solana.nft.listener.service.order

import com.rarible.core.entity.reducer.service.Reducer
import com.rarible.protocol.solana.common.event.OrderEvent
import com.rarible.protocol.solana.common.model.Order
import org.springframework.stereotype.Component

@Component
class ReversedOrderValueReducer : Reducer<OrderEvent, Order> {
    private val forwardValueTokenReducer = ForwardOrderReducer()

    override suspend fun reduce(entity: Order, event: OrderEvent): Order {
        val invert = event.invert() ?: return Order.empty()

        return forwardValueTokenReducer.reduce(entity, invert)
    }
}