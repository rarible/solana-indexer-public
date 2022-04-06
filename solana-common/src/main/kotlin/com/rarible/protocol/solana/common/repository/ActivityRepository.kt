package com.rarible.protocol.solana.common.repository

import com.rarible.core.apm.CaptureSpan
import com.rarible.core.apm.SpanType
import com.rarible.protocol.solana.common.continuation.DateIdContinuation
import com.rarible.protocol.solana.common.model.ActivityRecord
import com.rarible.protocol.solana.common.model.asRecord
import com.rarible.protocol.solana.dto.ActivityDto
import com.rarible.protocol.solana.dto.ActivityTypeDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.index.Index
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.stereotype.Repository

@Repository
@CaptureSpan(type = SpanType.DB)
class ActivityRepository(
    private val mongo: ReactiveMongoOperations,
) {

    suspend fun save(activity: ActivityDto): ActivityRecord =
        mongo.save(activity.asRecord(), COLLECTION).awaitFirst()

    suspend fun removeById(id: String): Boolean {
        val record = mongo.findById(id, ActivityRecord::class.java, COLLECTION)
        return mongo.remove(record, COLLECTION).awaitSingle().deletedCount == 1L
    }

    suspend fun findById(id: String): ActivityDto? =
        mongo.findById(id, ActivityRecord::class.java, COLLECTION).awaitFirstOrNull()?.toDto()

    fun findAllActivities(
        types: Collection<ActivityTypeDto>,
        continuation: DateIdContinuation?,
        size: Int,
        sortAscending: Boolean,
    ): Flow<ActivityDto> {
        val criteria = Criteria(ActivityRecord::type.name).`in`(types)
            .addContinuation(continuation, sortAscending)
        val query = Query(criteria)
            .with(Sort.by(ActivityRecord::date.name, "_id").direction(sortAscending))
            .limit(size)
        return mongo.find(query, ActivityRecord::class.java, COLLECTION)
            .map { it.toDto() }.asFlow()
    }

    fun findActivitiesByMint(
        types: Collection<ActivityTypeDto>,
        mint: String,
        continuation: DateIdContinuation?,
        size: Int,
        sortAscending: Boolean,
    ): Flow<ActivityDto> {
        val criteria = Criteria(ActivityRecord::type.name).`in`(types)
            .and(ActivityRecord::mint.name).isEqualTo(mint)
            .addContinuation(continuation, sortAscending)
        val query = Query(criteria)
            .with(Sort.by(ActivityRecord::date.name, "_id").direction(sortAscending))
            .limit(size)
        return mongo.find(query, ActivityRecord::class.java, COLLECTION).map { it.toDto() }.asFlow()
    }

    private fun Criteria.addContinuation(continuation: DateIdContinuation?, sortAscending: Boolean) = this.apply {
        continuation?.let { c ->
            if (sortAscending) {
                orOperator(
                    Criteria(ActivityRecord::date.name).isEqualTo(c.date).and("_id").gt(c.id),
                    Criteria(ActivityRecord::date.name).gt(c.date)
                )
            } else {
                orOperator(
                    Criteria(ActivityRecord::date.name).isEqualTo(c.date).and("_id").lt(c.id),
                    Criteria(ActivityRecord::date.name).lt(c.date)
                )
            }
        }
    }

    private fun Sort.direction(asc: Boolean) =
        if (asc) ascending() else descending()

    suspend fun createIndexes() {
        logger.info("Ensuring indexes on $COLLECTION")
        ALL_INDEXES.forEach { index ->
            mongo.indexOps(COLLECTION).ensureIndex(index).awaitFirst()
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ActivityRepository::class.java)
        private const val COLLECTION = "activity"

        private val TYPE_AND_MINT_AND_DATE_AND_ID = Index()
            .on(ActivityRecord::type.name, Sort.Direction.ASC)
            .on(ActivityRecord::mint.name, Sort.Direction.ASC)
            .on(ActivityRecord::date.name, Sort.Direction.ASC)
            .on("_id", Sort.Direction.ASC)
            .background()

        val ALL_INDEXES = listOf(
            TYPE_AND_MINT_AND_DATE_AND_ID,
        )
    }
}
