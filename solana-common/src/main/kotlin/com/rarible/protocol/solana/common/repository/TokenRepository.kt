package com.rarible.protocol.solana.common.repository

import com.rarible.protocol.solana.common.continuation.DateIdContinuation
import com.rarible.protocol.solana.common.model.Balance
import com.rarible.protocol.solana.common.model.Token
import com.rarible.protocol.solana.common.model.TokenId
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
import java.time.Instant

@Component
class TokenRepository(
    private val mongo: ReactiveMongoOperations
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    suspend fun save(token: Token): Token =
        mongo.save(token).awaitFirst()

    suspend fun findByMint(mint: TokenId): Token? =
        mongo.findById<Token>(mint).awaitFirstOrNull()

    suspend fun findByMints(mints: Collection<TokenId>): Flow<Token> =
        mongo.find(
            Query(Criteria.where("_id").`in`(mints))
                .with(Sort.by("_id")), Token::class.java
        ).asFlow()

    suspend fun findAll(
        lastUpdatedFrom: Instant?,
        lastUpdatedTo: Instant?,
        continuation: DateIdContinuation?,
        limit: Int
    ): Flow<Token> {

        val criteria = Criteria()
            .addContinuation(continuation)

        // in continuation date will always less than param form query
        // but in case if there is no continuation, we have to add this filter
        if (continuation == null && lastUpdatedTo != null) {
            criteria.and(Token::updatedAt.name).lt(lastUpdatedTo)
        }

        lastUpdatedFrom?.let { criteria.and(Token::updatedAt.name).gt(it) }

        val query = Query(criteria).withSortByLastUpdateAndId()
        query.limit(limit)

        return mongo.find(query, Token::class.java).asFlow()
    }

    private fun Query.withSortByLastUpdateAndId() =
        with(Sort.by(Sort.Direction.DESC, Token::updatedAt.name, "_id"))

    private fun Criteria.addContinuation(continuation: DateIdContinuation?) =
        continuation?.let {
            orOperator(
                Criteria(Token::updatedAt.name).isEqualTo(continuation.date).and("_id").lt(continuation.id),
                Criteria(Token::updatedAt.name).lt(continuation.date)
            )
        } ?: this

    suspend fun createIndexes() {
        logger.info("Ensuring indexes on ${Token.COLLECTION}")
        TokenIndexes.ALL_INDEXES.forEach { index ->
            mongo.indexOps(Balance.COLLECTION).ensureIndex(index).awaitFirst()
        }
    }

    private object TokenIndexes {

        val UPDATED_AT_AND_ID: Index = Index()
            .on(Balance::updatedAt.name, Sort.Direction.ASC)
            .on("_id", Sort.Direction.ASC)

        val ALL_INDEXES = listOf(
            UPDATED_AT_AND_ID
        )
    }
}
