package com.rarible.protocol.solana.common.repository

import com.rarible.protocol.solana.common.model.Balance
import com.rarible.protocol.solana.common.model.BalanceId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.findById
import org.springframework.data.mongodb.core.index.Index
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.stereotype.Component

@Component
class BalanceRepository(
    private val mongo: ReactiveMongoOperations
) {

    suspend fun save(balance: Balance): Balance =
        mongo.save(balance).awaitFirst()

    suspend fun findByAccount(account: BalanceId): Balance? =
        mongo.findById<Balance>(account).awaitFirstOrNull()

    fun findByOwner(owner: String): Flow<Balance> {
        val criteria = Criteria.where(Balance::owner.name).isEqualTo(owner)
        val query = Query(criteria).with(Sort.by(Balance::owner.name, "_id"))
        return mongo.find(query, Balance::class.java).asFlow()
    }

    fun findByMint(mint: String): Flow<Balance> {
        val criteria = Criteria.where(Balance::mint.name).isEqualTo(mint)
        val query = Query(criteria).with(Sort.by(Balance::mint.name, "_id"))
        return mongo.find(query, Balance::class.java).asFlow()
    }

    suspend fun createIndexes() {
        val logger = LoggerFactory.getLogger(BalanceRepository::class.java)
        logger.info("Ensuring indexes on ${Balance.COLLECTION}")
        BalanceIndexes.ALL_INDEXES.forEach { index ->
            mongo.indexOps(Balance.COLLECTION).ensureIndex(index).awaitFirst()
        }
    }

    private object BalanceIndexes {
        val OWNER_ID: Index = Index()
            .on(Balance::owner.name, Sort.Direction.ASC)
            .on("_id", Sort.Direction.ASC)

        val MINT_ID: Index = Index()
            .on(Balance::mint.name, Sort.Direction.ASC)
            .on("_id", Sort.Direction.ASC)

        val ALL_INDEXES = listOf(
            OWNER_ID,
            MINT_ID
        )
    }

}
