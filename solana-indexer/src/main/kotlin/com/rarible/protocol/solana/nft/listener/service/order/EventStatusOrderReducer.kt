package com.rarible.protocol.solana.nft.listener.service.order

import com.rarible.core.entity.reducer.service.Reducer
import com.rarible.protocol.solana.common.event.OrderEvent
import com.rarible.protocol.solana.common.model.Order
import org.springframework.stereotype.Component

@Component
class EventStatusOrderReducer(
    private val forwardChainOrderReducer: ForwardChainOrderReducer,
    private val reversedChainOrderReducer: ReversedChainOrderReducer,
) : Reducer<OrderEvent, Order> {

    override suspend fun reduce(entity: Order, event: OrderEvent): Order {
        return if (event.reversed) {
            reversedChainOrderReducer.reduce(entity, event)
        } else {
            forwardChainOrderReducer.reduce(entity, event)
        }
    }
}
