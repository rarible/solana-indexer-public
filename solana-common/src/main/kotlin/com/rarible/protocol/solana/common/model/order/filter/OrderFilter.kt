package com.rarible.protocol.solana.common.model.order.filter

import com.rarible.protocol.solana.common.model.Order
import com.rarible.protocol.solana.common.model.OrderStatus
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.and
import org.springframework.data.mongodb.core.query.inValues

sealed class OrderFilter {

    abstract fun getQuery(continuation: String?, limit: Int): Query
    abstract fun getNextContinuationFrom(last: Order): String?

    data class All(
        val statuses: List<OrderStatus>,
        val sort: OrderFilterSort
    ) : OrderFilter() {
        // TODO[orders]: respect [continuation].
        override fun getQuery(continuation: String?, limit: Int): Query =
            Query(
                Criteria()
                    .forStatuses(statuses)
            ).limit(limit).with(sort(sort))

        override fun getNextContinuationFrom(last: Order) = last.id
    }

    protected fun sort(sort: OrderFilterSort): Sort {
        return when (sort) {
            OrderFilterSort.LAST_UPDATE_ASC -> Sort.by(
                Sort.Direction.ASC,
                Order::updatedAt.name,
                Order::id.name
            )
            OrderFilterSort.LAST_UPDATE_DESC -> Sort.by(
                Sort.Direction.DESC,
                Order::updatedAt.name,
                Order::id.name
            )
        }
    }

    fun Criteria.forStatuses(status: List<OrderStatus>): Criteria =
        and(Order::status).inValues(status)
}