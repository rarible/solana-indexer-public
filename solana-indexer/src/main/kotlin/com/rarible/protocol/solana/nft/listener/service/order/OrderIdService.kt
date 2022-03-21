package com.rarible.protocol.solana.nft.listener.service.order

import com.rarible.core.entity.reducer.service.EntityIdService
import com.rarible.protocol.solana.common.event.OrderEvent
import com.rarible.protocol.solana.common.model.OrderId
import org.springframework.stereotype.Component

@Component
class OrderIdService : EntityIdService<OrderEvent, OrderId> {
    override fun getEntityId(event: OrderEvent): OrderId {
        return TODO() // id?
    }
}