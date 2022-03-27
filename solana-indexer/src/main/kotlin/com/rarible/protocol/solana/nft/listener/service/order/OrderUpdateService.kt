package com.rarible.protocol.solana.nft.listener.service.order

import com.rarible.core.entity.reducer.service.EntityService
import com.rarible.protocol.solana.common.model.Order
import com.rarible.protocol.solana.common.model.OrderId
import com.rarible.protocol.solana.common.repository.OrderRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class OrderUpdateService(
    private val repository: OrderRepository
) : EntityService<OrderId, Order> {

    override suspend fun get(id: OrderId): Order? =
        repository.findById(id)

    override suspend fun update(entity: Order): Order {
        if (entity == Order.empty()) {
            return entity
        }
        val order = repository.save(entity)
        logger.info("Updated order: $entity")
        return order
    }

    companion object {
        private val logger = LoggerFactory.getLogger(OrderUpdateService::class.java)
    }
}