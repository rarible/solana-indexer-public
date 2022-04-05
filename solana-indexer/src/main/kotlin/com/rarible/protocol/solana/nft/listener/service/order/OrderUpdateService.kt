package com.rarible.protocol.solana.nft.listener.service.order

import com.rarible.core.entity.reducer.service.EntityService
import com.rarible.protocol.solana.common.model.Order
import com.rarible.protocol.solana.common.model.OrderId
import com.rarible.protocol.solana.common.model.OrderStatus
import com.rarible.protocol.solana.common.records.OrderDirection
import com.rarible.protocol.solana.common.repository.BalanceRepository
import com.rarible.protocol.solana.common.repository.OrderRepository
import com.rarible.protocol.solana.common.update.OrderUpdateListener
import kotlinx.coroutines.flow.firstOrNull
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class OrderUpdateService(
    private val balanceRepository: BalanceRepository,
    private val orderRepository: OrderRepository,
    private val orderUpdateListener: OrderUpdateListener
) : EntityService<OrderId, Order> {

    override suspend fun get(id: OrderId): Order? =
        orderRepository.findById(id)

    override suspend fun update(entity: Order): Order {
        if (entity.createdAt == Instant.EPOCH) {
            // createdAt set only if we got initial order event with all fields,
            // if this field wasn't set, it means order doesn't contain major fields
            return entity
        }

        val updated = entity.checkForUpdates()
        val exist = orderRepository.findById(entity.id)

        if (!requireUpdate(updated, exist)) {
            // Nothing changed in order record
            logger.info("Order $entity not changed")
            return entity
        }

        val order = orderRepository.save(entity)
        logger.info("Updated order: $entity")

        orderUpdateListener.onOrderChanged(order)
        return order
    }

    private suspend fun Order.checkForUpdates(): Order {
        return checkBalance() // continue update chain if needed
    }

    private suspend fun Order.checkBalance(): Order {
        if (direction != OrderDirection.SELL) {
            return this // Do not check bid orders, only sell
        }
        if (status != OrderStatus.INACTIVE && status != OrderStatus.ACTIVE) {
            // Balance change can affect only ACTIVE/INACTIVE orders
            return this
        }

        val balance = balanceRepository.findByMintAndOwner(make.type.tokenAddress, maker)
            .firstOrNull()

        return if (balance == null || balance.value < make.amount) {
            this.copy(status = OrderStatus.INACTIVE)
        } else {
            this.copy(status = OrderStatus.ACTIVE)
        }
    }

    private fun requireUpdate(updated: Order, exist: Order?): Boolean {
        if (exist == null) return true

        // If nothing changed except updateAt, there is no sense to publish events
        return exist == updated.copy(updatedAt = exist.updatedAt)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(OrderUpdateService::class.java)
    }
}