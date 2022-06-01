package com.rarible.protocol.solana.nft.migration.mongock

import com.github.cloudyrock.mongock.ChangeLog
import com.github.cloudyrock.mongock.ChangeSet
import com.rarible.protocol.solana.common.model.ActivityRecord
import com.rarible.protocol.solana.common.repository.ActivityRepository
import io.changock.migration.api.annotations.NonLockGuarded
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.aggregation.AggregationUpdate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query

@ChangeLog(order = "00002")
class ChangeLog00002MarkOutcomeActivities {

    private val logger = LoggerFactory.getLogger(ChangeLog00002MarkOutcomeActivities::class.java)

    @ChangeSet(
        id = "ChangeLog00002MarkOutcomeActivities.updateActivityDbUpdateField",
        order = "00003",
        author = "protocol"
    )
    fun updateActivityDbUpdateField(
        @NonLockGuarded mongo: ReactiveMongoOperations
    ) = runBlocking<Unit> {
        val queryMulti = Query(Criteria.where(ActivityRecord::dbUpdatedAt.name).exists(false))
        val multiUpdate = AggregationUpdate.update()
            .set(ActivityRecord::dbUpdatedAt.name).toValue("\$${ActivityRecord::date.name}")
        mongo.updateMulti(queryMulti, multiUpdate, ActivityRepository.COLLECTION).awaitFirst()
    }
}