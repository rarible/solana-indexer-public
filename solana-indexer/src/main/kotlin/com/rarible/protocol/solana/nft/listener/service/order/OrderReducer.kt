package com.rarible.protocol.solana.nft.listener.service.order

import com.rarible.core.entity.reducer.chain.combineIntoChain
import com.rarible.core.entity.reducer.service.Reducer
import com.rarible.protocol.solana.common.event.OrderEvent
import com.rarible.protocol.solana.common.model.Order
import com.rarible.protocol.solana.nft.listener.service.LoggingReducer
import org.springframework.stereotype.Component

@Component
class OrderReducer(
    eventStatusOrderReducer: EventStatusOrderReducer,
    orderMetricReducer: OrderMetricReducer
) : Reducer<OrderEvent, Order> {

    private val eventStatusOrderReducer = combineIntoChain(
        LoggingReducer(),
        orderMetricReducer,
        eventStatusOrderReducer
    )

    override suspend fun reduce(entity: Order, event: OrderEvent): Order {
        return eventStatusOrderReducer.reduce(entity, event)
    }
}