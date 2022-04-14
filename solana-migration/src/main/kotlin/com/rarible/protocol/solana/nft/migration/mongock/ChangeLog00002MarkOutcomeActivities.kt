package com.rarible.protocol.solana.nft.migration.mongock

import com.github.cloudyrock.mongock.ChangeLog
import com.github.cloudyrock.mongock.ChangeSet
import com.rarible.protocol.solana.common.records.SubscriberGroup
import com.rarible.protocol.solana.common.repository.ActivityRepository
import com.rarible.protocol.solana.dto.ActivityTypeDto
import io.changock.migration.api.annotations.NonLockGuarded
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update

@ChangeLog(order = "00002")
class ChangeLog00002MarkOutcomeActivities {

    private val logger = LoggerFactory.getLogger(ChangeLog00002MarkOutcomeActivities::class.java)

    @ChangeSet(
        id = "ChangeLog00002MarkOutcomeActivities.markOutcomeActivities",
        order = "00002",
        author = "protocol"
    )
    fun markOutcomeActivities(
        @NonLockGuarded activityRepository: ActivityRepository,
        @NonLockGuarded mongo: ReactiveMongoOperations
    ) = runBlocking {
        var processed = 0
        activityRepository.findAllActivities(
            types = listOf(ActivityTypeDto.TRANSFER),
            continuation = null,
            size = Int.MAX_VALUE,
            sortAscending = true
        ).collect { activity ->
            if (activity.id.endsWith("outcome")) {

                val criteria = Criteria.where("_id").`is`(activity.id)

                mongo.updateFirst(
                    Query(criteria),
                    Update().set("outcome", "true"),
                    ActivityRepository.COLLECTION
                ).awaitFirst()
            }
            if (++processed % 10000 == 0) {
                logger.info("Processed $processed activities")
            }
        }
    }
}