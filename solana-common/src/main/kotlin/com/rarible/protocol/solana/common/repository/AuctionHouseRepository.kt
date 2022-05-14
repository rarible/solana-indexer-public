package com.rarible.protocol.solana.common.repository

import com.rarible.protocol.solana.common.model.AuctionHouse
import com.rarible.protocol.solana.common.model.AuctionHouseId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.findById
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.inValues
import org.springframework.stereotype.Component

@Component
class AuctionHouseRepository(
    private val mongo: ReactiveMongoOperations
) {
    suspend fun save(auctionHouse: AuctionHouse): AuctionHouse =
        mongo.save(auctionHouse).awaitFirst()

    suspend fun findByAccount(account: AuctionHouseId): AuctionHouse? =
        mongo.findById<AuctionHouse>(account).awaitFirstOrNull()

    fun findByAccounts(ids: Collection<AuctionHouseId>): Flow<AuctionHouse> {
        val query = Query(Criteria("_id").inValues(ids))

        return mongo.find(query, AuctionHouse::class.java).asFlow()
    }
}