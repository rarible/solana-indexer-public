package com.rarible.protocol.solana.common.repository

import com.rarible.protocol.solana.common.continuation.DateIdContinuation
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

    private val logger = LoggerFactory.getLogger(javaClass)

    suspend fun save(balance: Balance): Balance =
        mongo.save(balance).awaitFirst()

    suspend fun findByAccount(account: BalanceId): Balance? =
        mongo.findById<Balance>(account).awaitFirstOrNull()

    suspend fun findByMintAndOwner(mint: String, owner: String): Flow<Balance> {
        val criteria = Criteria().andOperator(
            Balance::owner isEqualTo owner,
            Balance::mint isEqualTo mint
        )
        val query = Query(criteria)
        return mongo.find(query, Balance::class.java).asFlow()
    }

    fun findByOwner(owner: String, continuation: DateIdContinuation?, limit: Int): Flow<Balance> {
        val criteria = Criteria
            .where(Balance::owner.name).isEqualTo(owner)
            .addContinuation(continuation)

        val query = Query(criteria).withSortByLastUpdateAndId()
        query.limit(limit)

        return mongo.find(query, Balance::class.java).asFlow()
    }

    fun findByMint(mint: String, continuation: DateIdContinuation?, limit: Int): Flow<Balance> {
        val criteria = Criteria
            .where(Balance::mint.name).isEqualTo(mint)
            .addContinuation(continuation)

        val query = Query(criteria).withSortByLastUpdateAndId()
        query.limit(limit)

        return mongo.find(query, Balance::class.java).asFlow()
    }

    private fun Query.withSortByLastUpdateAndId() =
        with(Sort.by(Sort.Direction.DESC, Balance::updatedAt.name, "_id"))

    private fun Criteria.addContinuation(continuation: DateIdContinuation?) =
        continuation?.let {
            orOperator(
                Criteria(Balance::updatedAt.name).isEqualTo(continuation.date).and("_id").lt(continuation.id),
                Criteria(Balance::updatedAt.name).lt(continuation.date)
            )
        } ?: this

    suspend fun createIndexes() {
        logger.info("Ensuring indexes on ${Balance.COLLECTION}")
        BalanceIndexes.ALL_INDEXES.forEach { index ->
            mongo.indexOps(Balance.COLLECTION).ensureIndex(index).awaitFirst()
        }
    }

    private object BalanceIndexes {

        val OWNER_AND_UPDATED_AT_AND_ID: Index = Index()
            .on(Balance::owner.name, Sort.Direction.ASC)
            .on(Balance::updatedAt.name, Sort.Direction.ASC)
            .on("_id", Sort.Direction.ASC)

        val MINT_AND_UPDATED_AT_AND_ID: Index = Index()
            .on(Balance::mint.name, Sort.Direction.ASC)
            .on(Balance::updatedAt.name, Sort.Direction.ASC)
            .on("_id", Sort.Direction.ASC)

        val OWNER_AND_MINT: Index = Index()
            .on(Balance::owner.name, Sort.Direction.ASC)
            .on(Balance::mint.name, Sort.Direction.ASC)

        val ALL_INDEXES = listOf(
            OWNER_AND_UPDATED_AT_AND_ID,
            MINT_AND_UPDATED_AT_AND_ID,
            OWNER_AND_MINT
        )
    }

}
