package com.rarible.protocol.solana.common.repository

import com.rarible.protocol.solana.common.records.SolanaAuctionHouseOrderRecord
import com.rarible.protocol.solana.common.records.SubscriberGroup
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitSingle
import org.jetbrains.annotations.TestOnly
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Component

@Component
class SolanaAuctionHouseOrderRecordsRepository(
    private val mongo: ReactiveMongoOperations,
) {

    @TestOnly
    suspend fun save(record: SolanaAuctionHouseOrderRecord): SolanaAuctionHouseOrderRecord =
        mongo.save(record, COLLECTION).awaitSingle()

    fun findBy(
        criteria: Criteria,
        size: Int? = null,
        asc: Boolean = true
    ): Flow<SolanaAuctionHouseOrderRecord> {
        val query = Query(criteria)
            .with(Sort.by(SolanaAuctionHouseOrderRecord::timestamp.name, "_id").direction(asc))
        size?.let(query::limit)

        return mongo.find(query, SolanaAuctionHouseOrderRecord::class.java, COLLECTION).asFlow()
    }

    private fun Sort.direction(asc: Boolean) =
        if (asc) ascending() else descending()

    companion object {
        private val COLLECTION = SubscriberGroup.AUCTION_HOUSE_ORDER.collectionName
    }
}
