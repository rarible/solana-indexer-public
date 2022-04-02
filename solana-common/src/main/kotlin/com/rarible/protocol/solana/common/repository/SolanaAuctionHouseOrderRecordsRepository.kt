package com.rarible.protocol.solana.common.repository

import com.rarible.protocol.solana.common.records.SolanaAuctionHouseOrderRecord
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
class SolanaAuctionHouseOrderRecordsRepository(
    private val mongo: ReactiveMongoOperations,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @TestOnly
    suspend fun save(record: SolanaAuctionHouseOrderRecord): SolanaAuctionHouseOrderRecord =
        mongo.save(record, COLLECTION).awaitSingle()

    fun findBy(
        criteria: Criteria,
        size: Int,
        asc: Boolean = true
    ): Flow<SolanaAuctionHouseOrderRecord> {
        val query = Query(criteria)
            .with(Sort.by("_id").direction(asc))
            .limit(size)
        return mongo.find(query, SolanaAuctionHouseOrderRecord::class.java, COLLECTION).asFlow()
    }

    suspend fun createIndexes() {
        logger.info("Ensuring indexes on $COLLECTION")
        ALL_INDEXES.forEach { index ->
            mongo.indexOps(COLLECTION).ensureIndex(index).awaitFirst()
        }
    }

    private fun Sort.direction(asc: Boolean) =
        if (asc) ascending() else descending()

    private companion object {
        private val COLLECTION = SubscriberGroup.AUCTION_HOUSE_ORDER.collectionName

        private val CLASS_ID: Index = Index()
            .on("_class", Sort.Direction.ASC)
            .on("_id", Sort.Direction.ASC)
            .background()

        private val ALL_INDEXES = listOf(
            CLASS_ID,
        )
    }

}
