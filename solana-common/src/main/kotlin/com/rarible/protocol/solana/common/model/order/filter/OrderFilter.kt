package com.rarible.protocol.solana.common.model.order.filter

import com.rarible.core.mongo.util.div
import com.rarible.protocol.solana.common.continuation.DateIdContinuation
import com.rarible.protocol.solana.common.continuation.PriceIdContinuation
import com.rarible.protocol.solana.common.model.Asset
import com.rarible.protocol.solana.common.model.AssetType
import com.rarible.protocol.solana.common.model.Order
import com.rarible.protocol.solana.common.model.OrderStatus
import com.rarible.protocol.solana.common.records.OrderDirection
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.and
import org.springframework.data.mongodb.core.query.gt
import org.springframework.data.mongodb.core.query.inValues
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.data.mongodb.core.query.lt
import java.time.Instant

sealed class OrderFilter {

    abstract fun getQuery(limit: Int): Query

    data class All(
        val sort: OrderFilterSort,
        val statuses: List<OrderStatus>?,
        val continuation: DateIdContinuation? = null
    ) : OrderFilter() {

        override fun getQuery(limit: Int): Query {
            val criteria = Criteria()
                .forStatuses(statuses)
                .addContinuation(continuation, sort)

            return Query(criteria)
                .limit(limit)
                .with(sort(sort))
        }
    }

    data class Sell(
        val sort: OrderFilterSort,
        val statuses: List<OrderStatus>? = null,
        val makers: List<String>? = null,
        val continuation: DateIdContinuation? = null
    ) : OrderFilter() {

        override fun getQuery(limit: Int): Query {
            val criteria = Criteria()
                .forDirection(OrderDirection.SELL)
                .forMakers(makers)
                .forStatuses(statuses)
                .addContinuation(continuation, sort)

            return Query(criteria)
                .limit(limit)
                .with(sort(sort))
        }
    }

    data class SellByItem(
        val statuses: List<OrderStatus>? = null,
        val currency: String,
        val tokenAddress: String,
        val makers: List<String>? = null,
        val continuation: PriceIdContinuation?
    ) : OrderFilter() {

        override fun getQuery(limit: Int): Query {
            val criteria = Criteria()
                .forSellTokenAddress(tokenAddress)
                .forMakers(makers)
                .forSellCurrency(currency)
                .forStatuses(statuses)
                .addSellContinuation(continuation)

            return Query(criteria)
                .limit(limit)
                .with(Sort.by(Sort.Direction.ASC, Order::makePrice.name))
        }

        private fun Criteria.forSellTokenAddress(tokenAddress: String): Criteria {
            return this.andOperator(
                Order::direction isEqualTo OrderDirection.SELL,
                Order::make / Asset::type / AssetType::tokenAddress isEqualTo tokenAddress,
            )
        }

        private fun Criteria.forSellCurrency(currency: String): Criteria {
            return and(Order::take / Asset::type / AssetType::tokenAddress).isEqualTo(currency)
        }

        private fun Criteria.addSellContinuation(continuation: PriceIdContinuation?): Criteria {
            val price = continuation?.price ?: return this
            val hash = continuation.id

            return this.orOperator(
                Order::makePrice gt price,
                Criteria().andOperator(
                    Order::makePrice isEqualTo price,
                    Order::id gt hash
                )
            )
        }
    }

    data class Buy(
        val sort: OrderFilterSort,
        val statuses: List<OrderStatus>? = null,
        val makers: List<String>? = null,
        val start: Instant? = null,
        val end: Instant? = null,
        val continuation: DateIdContinuation? = null
    ) : OrderFilter() {

        override fun getQuery(limit: Int): Query {
            val criteria = Criteria()
                .forMakers(makers)
                .forStatuses(statuses)
                .forCreatedAt(start, end)
                .addContinuation(continuation, sort)

            return Query(criteria)
                .limit(limit)
                .with(sort(sort))
        }
    }

    data class BuyByItem(
        val statuses: List<OrderStatus>? = null,
        val currency: String,
        val tokenAddress: String,
        val makers: List<String>? = null,
        val start: Instant? = null,
        val end: Instant? = null,
        val continuation: PriceIdContinuation? = null
    ) : OrderFilter() {

        override fun getQuery(limit: Int): Query {
            val criteria = Criteria()
                .forBuyTokenAddress(tokenAddress)
                .forMakers(makers)
                .forBuyCurrency(currency)
                .forStatuses(statuses)
                .forCreatedAt(start, end)
                .addBidContinuation(continuation)

            return Query(criteria)
                .limit(limit)
                .with(Sort.by(Sort.Direction.DESC, Order::takePrice.name))
        }

        private fun Criteria.forBuyTokenAddress(tokenAddress: String): Criteria {
            return this.andOperator(
                Order::direction isEqualTo OrderDirection.BUY,
                Order::take / Asset::type / AssetType::tokenAddress isEqualTo tokenAddress,
            )
        }

        private fun Criteria.forBuyCurrency(currency: String): Criteria {
            return and(Order::make / Asset::type / AssetType::tokenAddress).isEqualTo(currency)
        }

        private fun Criteria.addBidContinuation(continuation: PriceIdContinuation?): Criteria {
            val price = continuation?.price ?: return this
            val hash = continuation.id

            return this.orOperator(
                Order::takePrice lt price,
                Criteria().andOperator(
                    Order::takePrice isEqualTo price,
                    Order::id lt hash
                )
            )
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

    fun Criteria.forMakers(maker: List<String>?): Criteria {
        return if (maker.isNullOrEmpty()) this else and(Order::maker).inValues(maker)
    }

    fun Criteria.forDirection(direction: OrderDirection?) = direction?.let {
        and(Order::direction).isEqualTo(it)
    } ?: this

    fun Criteria.forCreatedAt(startDate: Instant?, endDate: Instant?): Criteria {
        if (startDate != null && endDate != null) {
            return and(Order::createdAt).gte(startDate).lte(endDate)
        }

        if (startDate != null) return and(Order::createdAt).gte(startDate)
        if (endDate != null) return and(Order::createdAt).lte(endDate)

        return this
    }

    fun Criteria.addContinuation(continuation: DateIdContinuation?, sort: OrderFilterSort) =
        continuation?.let {
            if (sort == OrderFilterSort.LAST_UPDATE_DESC) {
                orOperator(
                    Criteria(Order::updatedAt.name).isEqualTo(continuation.date).and("_id").lt(continuation.id),
                    Criteria(Order::updatedAt.name).lt(continuation.date)
                )
            } else {
                orOperator(
                    Criteria(Order::updatedAt.name).isEqualTo(continuation.date).and("_id").gt(continuation.id),
                    Criteria(Order::updatedAt.name).gt(continuation.date)
                )
            }
        } ?: this

}