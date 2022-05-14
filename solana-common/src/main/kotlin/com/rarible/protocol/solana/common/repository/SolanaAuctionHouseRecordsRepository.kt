package com.rarible.protocol.solana.common.repository

import com.rarible.core.apm.CaptureSpan
import com.rarible.core.apm.SpanType
import com.rarible.protocol.solana.common.records.SolanaAuctionHouseRecord
import com.rarible.protocol.solana.common.records.SubscriberGroup
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirst
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.index.Index
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Component

@Component
@CaptureSpan(type = SpanType.DB)
class SolanaAuctionHouseRecordsRepository(
    private val mongo: ReactiveMongoOperations,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun findBy(
        criteria: Criteria,
        sort: Sort,
        size: Int? = null
    ): Flow<SolanaAuctionHouseRecord> {
        val query = Query(criteria).with(sort)
        if (size != null) query.limit(size)

        return mongo.find(query, SolanaAuctionHouseRecord::class.java, COLLECTION).asFlow()
    }

    suspend fun createIndexes() {
        logger.info("Ensuring indexes on $COLLECTION")
        ALL.forEach { index ->
            mongo.indexOps(COLLECTION).ensureIndex(index).awaitFirst()
        }
    }

    private companion object {
        private val COLLECTION = SubscriberGroup.AUCTION_HOUSE.collectionName

        private val ALL = emptyList<Index>()
    }
}
