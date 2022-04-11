package com.rarible.protocol.solana.nft.listener.service.order

import com.rarible.core.entity.reducer.service.Reducer
import com.rarible.protocol.solana.common.event.OrderEvent
import com.rarible.protocol.solana.common.model.Order
import org.springframework.stereotype.Component

@Component
class ReversedOrderValueReducer : Reducer<OrderEvent, Order> {
    override suspend fun reduce(entity: Order, event: OrderEvent): Order {

        val newEntity = entity.states.lastOrNull() ?: return Order.empty()
        val newStates = entity.states.dropLast(1)

        return newEntity.copy(states = newStates)
    }
}