package com.rarible.protocol.solana.common.repository

import com.rarible.core.apm.CaptureSpan
import com.rarible.core.apm.SpanType
import com.rarible.protocol.solana.common.records.SolanaBalanceRecord
import com.rarible.protocol.solana.common.records.SubscriberGroup
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitSingle
import org.jetbrains.annotations.TestOnly
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.index.Index
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Component

@Component
@CaptureSpan(type = SpanType.DB)
class SolanaBalanceRecordsRepository(
    private val mongo: ReactiveMongoOperations,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @TestOnly
    suspend fun save(record: SolanaBalanceRecord): SolanaBalanceRecord =
        mongo.save(record, COLLECTION).awaitSingle()

    fun findBy(
        criteria: Criteria,
        size: Int? = null,
        asc: Boolean = true
    ): Flow<SolanaBalanceRecord> {
        val query = Query(criteria)
            .with(Sort.by(SolanaBalanceRecord::timestamp.name, "_id").direction(asc))
        size?.let(query::limit)

        return mongo.find(query, SolanaBalanceRecord::class.java, COLLECTION).asFlow()
    }

    private fun Sort.direction(asc: Boolean) =
        if (asc) ascending() else descending()

    suspend fun createIndices() {
        ALL.forEach { index ->
            logger.info("Ensure index '{}' for collection '{}'", index, COLLECTION)
            mongo.indexOps(COLLECTION).ensureIndex(index).awaitFirst()
        }
    }

    companion object {
        private val COLLECTION = SubscriberGroup.BALANCE.collectionName

        private val BALANCE_ACTIVITY_BY_ITEM: Index = Index()
            .on("_class", Sort.Direction.ASC)
            .on("mint", Sort.Direction.ASC)
            .on("_id", Sort.Direction.ASC)
            .background()

        private val BALANCE_ACTIVITY_ALL: Index = Index()
            .on("_class", Sort.Direction.DESC)
            .on("_id", Sort.Direction.DESC)
            .background()

        private val ALL = listOf(
            BALANCE_ACTIVITY_BY_ITEM,
            BALANCE_ACTIVITY_ALL,
        )
    }
}
