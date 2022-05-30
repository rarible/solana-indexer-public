package com.rarible.protocol.solana.common.repository

import com.rarible.core.apm.CaptureSpan
import com.rarible.core.apm.SpanType
import com.rarible.protocol.solana.common.continuation.DateIdContinuation
import com.rarible.protocol.solana.common.continuation.IdContinuation
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
        mongo.save(activity.asRecord().withDbUpdatedAt(), COLLECTION).awaitFirst()


    suspend fun removeById(id: String): Boolean {
        val record = mongo.findById(id, ActivityRecord::class.java, COLLECTION)
        return mongo.remove(record, COLLECTION).awaitSingle().deletedCount == 1L
    }

    suspend fun findById(id: String): ActivityDto? =
        mongo.findById(id, ActivityRecord::class.java, COLLECTION).awaitFirstOrNull()?.toDto()

    fun findByIds(ids:List<String>): Flow<ActivityDto> {
        val criteria = Criteria.where("_id").`in`(ids)
        return mongo.find(Query(criteria), ActivityRecord::class.java, COLLECTION)
            .map { it.toDto() }.asFlow()
    }

    fun findAllActivitiesSync(
        continuation: DateIdContinuation?,
        size: Int,
        sortAscending: Boolean,
    ): Flow<ActivityDto> {
        val criteria = Criteria()
            .addSyncContinuation(continuation, sortAscending)
        val query = Query(criteria)
            .with(Sort.by(ActivityRecord::dbUpdatedAt.name).direction(sortAscending))
            .limit(size)
        return mongo.find(query, ActivityRecord::class.java, COLLECTION)
            .map { it.toDto() }.asFlow()
    }

    fun findAllActivities(
        types: Collection<ActivityTypeDto>,
        continuation: IdContinuation?,
        size: Int?,
        sortAscending: Boolean,
    ): Flow<ActivityDto> {
        val criteria = Criteria(ActivityRecord::type.name).`in`(types)
            .addContinuation(continuation, sortAscending)
        val query = Query(criteria)
            .with(Sort.by("_id").direction(sortAscending))
        if (size != null) {
            query.limit(size)
        }
        return mongo.find(query, ActivityRecord::class.java, COLLECTION)
            .map { it.toDto() }.asFlow()
    }

    fun findActivitiesByMint(
        types: Collection<ActivityTypeDto>,
        mint: String,
        continuation: IdContinuation?,
        size: Int,
        sortAscending: Boolean,
    ): Flow<ActivityDto> {
        val criteria = Criteria(ActivityRecord::type.name).`in`(types)
            .and(ActivityRecord::mint.name).isEqualTo(mint)
            .addContinuation(continuation, sortAscending)
        val query = Query(criteria)
            .with(Sort.by("_id").direction(sortAscending))
            .limit(size)
        return mongo.find(query, ActivityRecord::class.java, COLLECTION).map { it.toDto() }.asFlow()
    }

    private fun Criteria.addContinuation(
        continuation: IdContinuation?,
        sortAscending: Boolean
    ) = if (continuation == null) {
        this
    } else {
        if (sortAscending) {
            and("_id").gt(continuation.id)
        } else {
            and("_id").lt(continuation.id)
        }
    }

    private fun Criteria.addSyncContinuation(
        continuation: DateIdContinuation?,
        sortAscending: Boolean
    ) = continuation?.let {
        if (sortAscending) {
            orOperator(
                Criteria(ActivityRecord::dbUpdatedAt.name).isEqualTo(continuation.date).and("_id").gt(continuation.id),
                Criteria(ActivityRecord::dbUpdatedAt.name).gt(continuation.date)
            )
        } else {
            orOperator(
                Criteria(ActivityRecord::dbUpdatedAt.name).isEqualTo(continuation.date).and("_id").lt(continuation.id),
                Criteria(ActivityRecord::dbUpdatedAt.name).lt(continuation.date)
            )

        }
    } ?: this

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
        const val COLLECTION = "activity"

        private val TYPE_AND_ID = Index()
            .on(ActivityRecord::type.name, Sort.Direction.ASC)
            .on("_id", Sort.Direction.ASC)
            .background()

        private val TYPE_AND_MINT_AND_ID = Index()
            .on(ActivityRecord::type.name, Sort.Direction.ASC)
            .on(ActivityRecord::mint.name, Sort.Direction.ASC)
            .on("_id", Sort.Direction.ASC)
            .background()

        private val DB_UPDATED_AT_AND_ID = Index()
            .on(ActivityRecord::dbUpdatedAt.name, Sort.Direction.ASC)
            .on("_id", Sort.Direction.ASC)
            .background()

        val ALL_INDEXES = listOf(
            TYPE_AND_ID,
            TYPE_AND_MINT_AND_ID,
            DB_UPDATED_AT_AND_ID
        )
    }
}
