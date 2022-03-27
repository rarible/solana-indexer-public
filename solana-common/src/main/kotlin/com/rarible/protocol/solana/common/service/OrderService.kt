package com.rarible.protocol.solana.common.service

import com.rarible.protocol.solana.common.model.Order
import com.rarible.protocol.solana.common.model.OrderId
import com.rarible.protocol.solana.common.repository.OrderRepository
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Component

@Component
class OrderService(
    private val repository: OrderRepository
) {

    suspend fun findById(id: OrderId): Order? {
        return repository.findById(id)
    }

    suspend fun findByIds(ids: List<OrderId>): List<Order> {
        return repository.findByIds(ids).toList()
    }
}