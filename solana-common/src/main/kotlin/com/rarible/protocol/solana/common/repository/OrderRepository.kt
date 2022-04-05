package com.rarible.protocol.solana.common.repository

import com.rarible.core.mongo.util.div
import com.rarible.protocol.solana.common.model.Asset
import com.rarible.protocol.solana.common.model.AssetType
import com.rarible.protocol.solana.common.model.Order
import com.rarible.protocol.solana.common.model.OrderId
import com.rarible.protocol.solana.common.model.OrderStatus
import com.rarible.protocol.solana.common.records.OrderDirection
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
import org.springframework.data.mongodb.core.query.isEqualTo
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

    fun findCurrencyTypesOfSellOrders(tokenAddress: String): Flow<AssetType> {
        val criteria = Criteria().andOperator(
            Order::direction isEqualTo OrderDirection.SELL,
            Order::make / Asset::type / AssetType::tokenAddress isEqualTo tokenAddress,
        )
        return mongo.findDistinct(
            Query(criteria),
            "${Order::take.name}.${Asset::type.name}",
            Order::class.java,
            AssetType::class.java
        ).asFlow()
    }

    fun findCurrencyTypesOfBuyOrders(tokenAddress: String): Flow<AssetType> {
        val criteria = Criteria().andOperator(
            Order::direction isEqualTo OrderDirection.BUY,
            Order::take / Asset::type / AssetType::tokenAddress isEqualTo tokenAddress,
        )
        return mongo.findDistinct(
            Query(criteria),
            "${Order::make.name}.${Asset::type.name}",
            Order::class.java,
            AssetType::class.java
        ).asFlow()
    }

    fun findSellOrdersByMintAndMaker(tokenAddress: String, maker: String, statuses: List<OrderStatus>): Flow<Order> {
        val criteria = Criteria().andOperator(
            Order::direction isEqualTo OrderDirection.SELL,
            Order::maker isEqualTo maker,
            Order::make / Asset::type / AssetType::tokenAddress isEqualTo tokenAddress,
            Order::status inValues statuses
        )
        val query = Query(criteria)
        return mongo.query<Order>().matching(query).all().asFlow()
    }

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

        val ALL_BY_STATUS_DEFINITION = Index()
            .on(Order::status.name, Sort.Direction.ASC)
            .on(Order::updatedAt.name, Sort.Direction.ASC)
            .on("_id", Sort.Direction.ASC)
            .background()

        val SELL_BUY_ORDERS_DEFINITION = Index()
            .on(Order::direction.name, Sort.Direction.ASC)
            .on(Order::updatedAt.name, Sort.Direction.ASC)
            .on("_id", Sort.Direction.ASC)
            .background()

        val SELL_BUY_ORDERS_BY_MAKER_DEFINITION = Index()
            .on(Order::maker.name, Sort.Direction.ASC)
            .on(Order::direction.name, Sort.Direction.ASC)
            .on(Order::updatedAt.name, Sort.Direction.ASC)
            .on("_id", Sort.Direction.ASC)
            .background()

        val SELL_BUY_ORDERS_BY_MAKER_AND_MINT_AND_STATUS_DEFINITION = Index()
            .on(Order::maker.name, Sort.Direction.ASC)
            .on("${Order::make.name}.${Asset::type.name}.${AssetType::tokenAddress.name}", Sort.Direction.ASC)
            .on(Order::direction.name, Sort.Direction.ASC)
            .on(Order::status.name, Sort.Direction.ASC)
            .background()

        // Best sell order by status
        val SELL_ORDERS_BY_ITEM_CURRENCY_STATUS_SORT_BY_PRICE_DEFINITION = Index()
            .on("${Order::make.name}.${Asset::type.name}.${AssetType::tokenAddress.name}", Sort.Direction.ASC)
            .on("${Order::take.name}.${Asset::type.name}.${AssetType::tokenAddress.name}", Sort.Direction.ASC)
            .on(Order::status.name, Sort.Direction.ASC)
            .on(Order::makePrice.name, Sort.Direction.ASC)
            .on("_id", Sort.Direction.ASC)
            .background()

        // Best bid order by status
        val BUY_ORDERS_BY_ITEM_CURRENCY_STATUS_SORT_BY_PRICE_DEFINITION = Index()
            .on("${Order::take.name}.${Asset::type.name}.${AssetType::tokenAddress.name}", Sort.Direction.ASC)
            .on("${Order::make.name}.${Asset::type.name}.${AssetType::tokenAddress.name}", Sort.Direction.ASC)
            .on(Order::status.name, Sort.Direction.ASC)
            .on(Order::takePrice.name, Sort.Direction.ASC)
            .on("_id", Sort.Direction.ASC)
            .background()

        val ALL_INDEXES = listOf(
            BY_UPDATED_AT_AND_ID_DEFINITION,
            ALL_BY_STATUS_DEFINITION,
            SELL_BUY_ORDERS_DEFINITION,
            SELL_BUY_ORDERS_BY_MAKER_DEFINITION,
            SELL_BUY_ORDERS_BY_MAKER_AND_MINT_AND_STATUS_DEFINITION,
            SELL_ORDERS_BY_ITEM_CURRENCY_STATUS_SORT_BY_PRICE_DEFINITION,
            BUY_ORDERS_BY_ITEM_CURRENCY_STATUS_SORT_BY_PRICE_DEFINITION
        )
    }
}
