package com.rarible.protocol.solana.nft.listener.service.order

import com.rarible.core.entity.reducer.service.EntityService
import com.rarible.protocol.solana.common.model.Order
import com.rarible.protocol.solana.common.model.OrderId
import com.rarible.protocol.solana.common.repository.OrderRepository
import com.rarible.protocol.solana.common.update.OrderUpdateListener
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class OrderUpdateService(
    private val repository: OrderRepository,
    private val orderUpdateListener: OrderUpdateListener
) : EntityService<OrderId, Order> {

    override suspend fun get(id: OrderId): Order? =
        repository.findById(id)

    override suspend fun update(entity: Order): Order {
        if (entity.createdAt == Instant.EPOCH) {
            // createdAt set only if we got initial order event with all fields,
            // if this field wasn't set, it means order doesn't contain major fields
            return entity
        }
        val order = repository.save(entity)
        logger.info("Updated order: $entity")

        // TODO do we need here any conditions?
        orderUpdateListener.onOrderChanged(order)
        return order
    }

    companion object {
        private val logger = LoggerFactory.getLogger(OrderUpdateService::class.java)
    }
}