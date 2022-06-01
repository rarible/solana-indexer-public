package com.rarible.protocol.solana.common.repository

import com.rarible.core.apm.CaptureSpan
import com.rarible.core.apm.SpanType
import com.rarible.protocol.solana.common.continuation.DateIdContinuation
import com.rarible.protocol.solana.common.continuation.IdContinuation
import com.rarible.protocol.solana.common.model.ActivityRecord
import com.rarible.protocol.solana.common.model.asRecord
import com.rarible.protocol.solana.dto.ActivityDto
import com.rarible.protocol.solana.dto.ActivityTypeDto
import com.rarible.protocol.solana.dto.SyncTypeDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import org.slf4j.LoggerFactory
import org.springframework.dao.DuplicateKeyException
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.index.Index
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.inValues
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.stereotype.Repository
import reactor.kotlin.core.publisher.toMono

@Repository
@CaptureSpan(type = SpanType.DB)
class ActivityRepository(
    private val mongo: ReactiveMongoOperations,
) {

    private val logger = LoggerFactory.getLogger(ActivityRepository::class.java)

    suspend fun save(
        activity: ActivityDto,
        collection: String = COLLECTION
    ): ActivityRecord =
        mongo.save(activity.asRecord().withDbUpdatedAt(), collection).awaitFirst()

    suspend fun saveAll(
        activities: List<ActivityDto>,
        collection: String = COLLECTION
    ): List<ActivityRecord> {
        if (activities.isEmpty()) {
            return emptyList()
        }
        return activities.map { save(it, collection) }
    }

    /**
     * Alternative to [saveAll] that has a better performance when inserting new documents.
     * Also, this function allows to override the target collection.
     * Use it only if none of the [activities] are present in [collectionName], otherwise the function
     * will fall back to saving one-by-one.
     */
    suspend fun insertAll(
        activities: List<ActivityDto>,
        collectionName: String
    ): List<ActivityRecord> {
        if (activities.isEmpty()) {
            return emptyList()
        }
        return try {
            mongo.insertAll(activities.map { it.asRecord() }.toMono(), collectionName).asFlow().toList()
        } catch (e: DuplicateKeyException) {
            logger.info("ActivityRepository.insertAll failed on collection $collectionName because of DuplicateKeyException, falling back to 'saveAll'")
            saveAll(activities, collectionName)
        }
    }

    suspend fun removeById(id: String): Boolean =
        removeByIds(listOf(id)) == 1L

    suspend fun removeByIds(
        ids: Collection<String>,
        collection: String = COLLECTION
    ): Long {
        if (ids.isEmpty()) {
            return 0
        }
        val criteria = Criteria("_id").inValues(ids)
        return mongo.remove(Query(criteria), collection).awaitSingle().deletedCount
    }

    suspend fun findById(id: String): ActivityDto? =
        mongo.findById(id, ActivityRecord::class.java, COLLECTION).awaitFirstOrNull()?.toDto()

    fun findByIds(ids: List<String>): Flow<ActivityDto> {
        val criteria = Criteria.where("_id").`in`(ids)
        return mongo.find(Query(criteria), ActivityRecord::class.java, COLLECTION)
            .map { it.toDto() }.asFlow()
    }

    fun findAllActivitiesSync(
        type: SyncTypeDto?,
        continuation: DateIdContinuation?,
        size: Int,
        sortAscending: Boolean
    ): Flow<ActivityDto> {
        val criteria = Criteria()
            .addSyncFilter(type)
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
    ): Flow<ActivityDto> = findActivitiesByMints(types, listOf(mint), continuation, size, sortAscending)

    fun findActivitiesByMints(
        types: Collection<ActivityTypeDto>,
        mints: Collection<String>,
        continuation: IdContinuation?,
        size: Int?,
        sortAscending: Boolean,
    ): Flow<ActivityDto> {
        val criteria = Criteria(ActivityRecord::type.name).`in`(types)
            .and(ActivityRecord::mint.name).`in`(mints)
            .addContinuation(continuation, sortAscending)
        val query = Query(criteria)
            .with(Sort.by("_id").direction(sortAscending))
        if (size != null) {
            query.limit(size)
        }
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

    private fun Criteria.addSyncFilter(
        type: SyncTypeDto?
    ) = when (type) {
        SyncTypeDto.AUCTION -> andOperator(syncFilterCriteria(AUCTION_TYPES))
        SyncTypeDto.ORDER -> andOperator(syncFilterCriteria(ORDER_TYPES))
        SyncTypeDto.NFT -> andOperator(syncFilterCriteria(NFT_TYPES))
        null -> this
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

        private val AUCTION_TYPES = setOf(
            ActivityTypeDto.AUCTION_BID,
            ActivityTypeDto.AUCTION_CANCEL,
            ActivityTypeDto.AUCTION_CREATED,
            ActivityTypeDto.AUCTION_ENDED,
            ActivityTypeDto.AUCTION_FINISHED,
            ActivityTypeDto.AUCTION_STARTED
        )

        private val NFT_TYPES = setOf(
            ActivityTypeDto.TRANSFER,
            ActivityTypeDto.MINT,
            ActivityTypeDto.BURN
        )

        private val ORDER_TYPES = setOf(
            ActivityTypeDto.BID,
            ActivityTypeDto.LIST,
            ActivityTypeDto.SELL,
            ActivityTypeDto.CANCEL_LIST,
            ActivityTypeDto.CANCEL_BID
        )

        private fun syncFilterCriteria(types: Set<ActivityTypeDto>): Criteria {
            return Criteria(ActivityRecord::type.name).`in`(types)
        }

        private val TYPE_AND_ID = Index()
            .on(ActivityRecord::type.name, Sort.Direction.ASC)
            .on("_id", Sort.Direction.ASC)
            .background()

        private val TYPE_AND_MINT_AND_ID = Index()
            .on(ActivityRecord::type.name, Sort.Direction.ASC)
            .on(ActivityRecord::mint.name, Sort.Direction.ASC)
            .on("_id", Sort.Direction.ASC)
            .background()

        private val TYPE_AND_DB_UPDATED_AT_AND_ID = Index()
            .on(ActivityRecord::type.name, Sort.Direction.ASC)
            .on(ActivityRecord::dbUpdatedAt.name, Sort.Direction.ASC)
            .on("_id", Sort.Direction.ASC)
            .background()

        private val DB_UPDATED_AT_AND_ID = Index()
            .on(ActivityRecord::dbUpdatedAt.name, Sort.Direction.ASC)
            .on("_id", Sort.Direction.ASC)
            .background()

        val ALL_INDEXES = listOf(
            TYPE_AND_ID,
            TYPE_AND_MINT_AND_ID,
            DB_UPDATED_AT_AND_ID,
            TYPE_AND_DB_UPDATED_AT_AND_ID
        )
    }
}
