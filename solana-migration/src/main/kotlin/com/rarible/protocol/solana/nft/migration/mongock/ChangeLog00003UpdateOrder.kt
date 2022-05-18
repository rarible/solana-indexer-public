package com.rarible.protocol.solana.nft.migration.mongock

import com.github.cloudyrock.mongock.ChangeLog
import com.github.cloudyrock.mongock.ChangeSet
import com.rarible.protocol.solana.common.model.Order
import io.changock.migration.api.annotations.NonLockGuarded
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.runBlocking
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.aggregation.AggregationUpdate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query

@ChangeLog(order = "00003")
class ChangeLog00003UpdateOrder {

    @ChangeSet(
        id = "ChangeLog00003UpdateOrder.updateOrderDbUpdateField",
        order = "00001",
        author = "protocol"
    )
    fun updateOrderDbUpdateField(
        @NonLockGuarded mongo: ReactiveMongoOperations
    ) = runBlocking<Unit> {
        val queryMulti = Query(Criteria.where(Order::dbUpdatedAt.name).exists(false))
        val multiUpdate = AggregationUpdate.update()
            .set(Order::dbUpdatedAt.name).toValue("\$${Order::updatedAt.name}")
        mongo.updateMulti(queryMulti, multiUpdate, Order.COLLECTION).awaitFirst()
    }
}