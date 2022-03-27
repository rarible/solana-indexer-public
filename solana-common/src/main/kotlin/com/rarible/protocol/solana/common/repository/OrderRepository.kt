package com.rarible.protocol.solana.common.repository

import com.rarible.protocol.solana.common.model.Order
import com.rarible.protocol.solana.common.model.OrderId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.findById
import org.springframework.data.mongodb.core.index.Index
import org.springframework.data.mongodb.core.query
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.inValues
import org.springframework.stereotype.Component

@Component
class OrderRepository(
    private val mongo: ReactiveMongoOperations
) {

    suspend fun findById(id: OrderId): Order? =
        mongo.findById<Order>(id).awaitFirstOrNull()

    fun findByIds(ids: Collection<OrderId>): Flow<Order> {
        val query = Query(Criteria("_id").inValues(ids))
        return mongo.find(query, Order::class.java).asFlow()
    }

    suspend fun save(order: Order): Order =
        mongo.save(order).awaitFirst()

    fun query(query: Query): Flow<Order> =
        mongo.query<Order>()
            .matching(query)
            .all()
            .asFlow()

    suspend fun createIndexes() {
        val logger = LoggerFactory.getLogger(OrderRepository::class.java)
        logger.info("Ensuring indexes on ${Order.COLLECTION}")
        OrderIndexes.ALL_INDEXES.forEach { index ->
            mongo.indexOps(Order.COLLECTION).ensureIndex(index).awaitFirst()
        }
    }

    private object OrderIndexes {

        val BY_UPDATED_AT_AND_ID_DEFINITION = Index()
            .on(Order::updatedAt.name, Sort.Direction.ASC)
            .on("_id", Sort.Direction.ASC)
            .background()

        val ALL_INDEXES = listOf(
            BY_UPDATED_AT_AND_ID_DEFINITION
        )
    }
}
