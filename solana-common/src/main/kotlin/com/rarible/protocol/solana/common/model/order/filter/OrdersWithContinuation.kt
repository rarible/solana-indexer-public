package com.rarible.protocol.solana.common.model.order.filter

import com.rarible.protocol.solana.common.model.Order

data class OrdersWithContinuation(
    val orders: List<Order>,
    val continuation: String?
)