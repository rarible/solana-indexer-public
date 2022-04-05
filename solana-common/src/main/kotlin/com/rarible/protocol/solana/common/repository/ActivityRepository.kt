package com.rarible.protocol.solana.common.repository

import com.rarible.protocol.solana.common.continuation.DateIdContinuation
import com.rarible.protocol.solana.common.model.ActivityRecord
import com.rarible.protocol.solana.common.model.asRecord
import com.rarible.protocol.solana.dto.ActivityDto
import com.rarible.protocol.solana.dto.ActivityTypeDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.stereotype.Repository

@Repository
class ActivityRepository(
    private val mongo: ReactiveMongoOperations,
) {

    suspend fun save(activity: ActivityDto): ActivityRecord =
        mongo.save(activity.asRecord()!!, COLLECTION).awaitFirst()

    suspend fun findById(id: String): ActivityDto? {
        return mongo.findById(id, ActivityRecord::class.java, COLLECTION).awaitFirstOrNull()?.toDto()
    }

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

    fun findActivitiesByItem(
        types: Collection<ActivityTypeDto>,
        itemId: String,
        continuation: DateIdContinuation?,
        size: Int,
        sortAscending: Boolean,
    ): Flow<ActivityDto> {
        val criteria = Criteria(ActivityRecord::type.name).`in`(types)
            .and(ActivityRecord::mint.name).isEqualTo(itemId)
            .addContinuation(continuation, sortAscending)
        val query = Query(criteria)
            .with(Sort.by(ActivityRecord::date.name, "_id").direction(sortAscending))
            .limit(size)
        println(types)
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

    companion object {
        private const val COLLECTION = "activity"
    }
}
