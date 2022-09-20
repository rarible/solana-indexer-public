package com.rarible.protocol.solana.nft.listener.service.order

import com.rarible.core.entity.reducer.service.EntityTemplateProvider
import com.rarible.protocol.solana.common.model.Order
import com.rarible.protocol.solana.common.model.OrderId
import org.springframework.stereotype.Component

@Component
class OrderTemplateProvider : EntityTemplateProvider<OrderId, Order> {
    override fun getEntityTemplate(id: OrderId, version: Long?): Order = Order.empty(version)
}