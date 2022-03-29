package com.rarible.protocol.solana.common.model.order.filter

import com.rarible.protocol.solana.common.continuation.DateIdContinuation
import com.rarible.protocol.solana.common.model.Order
import com.rarible.protocol.solana.common.model.OrderStatus
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.and
import org.springframework.data.mongodb.core.query.inValues
import org.springframework.data.mongodb.core.query.isEqualTo

sealed class OrderFilter {

    abstract fun getQuery(limit: Int): Query

    data class All(
        val statuses: List<OrderStatus>?,
        val sort: OrderFilterSort,
        val continuation: DateIdContinuation?
    ) : OrderFilter() {

        override fun getQuery(limit: Int): Query {
            val criteria = Criteria()
                .forStatuses(statuses)
                .addContinuation(continuation)

            return Query(criteria)
                .limit(limit)
                .with(sort(sort))
        }
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

    fun Criteria.forStatuses(status: List<OrderStatus>?): Criteria =
        if (status.isNullOrEmpty()) this else and(Order::status).inValues(status)

    fun Criteria.addContinuation(continuation: DateIdContinuation?) =
        continuation?.let {
            orOperator(
                Criteria(Order::updatedAt.name).isEqualTo(continuation.date).and("_id").lt(continuation.id),
                Criteria(Order::updatedAt.name).lt(continuation.date)
            )
        } ?: this
}