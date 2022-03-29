package com.rarible.protocol.solana.nft.listener.service.order

import com.rarible.core.entity.reducer.service.Reducer
import com.rarible.protocol.solana.common.event.OrderEvent
import com.rarible.protocol.solana.common.model.Order
import com.rarible.protocol.solana.common.service.PriceNormalizer
import org.springframework.stereotype.Component

@Component
class ReversedOrderValueReducer(priceNormalizer: PriceNormalizer) : Reducer<OrderEvent, Order> {
    private val forwardValueTokenReducer = ForwardOrderReducer(priceNormalizer)

    override suspend fun reduce(entity: Order, event: OrderEvent): Order {
        val invert = event.invert() ?: return Order.empty()

        return forwardValueTokenReducer.reduce(entity, invert)
    }
}