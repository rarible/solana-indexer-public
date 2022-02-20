package com.rarible.protocol.solana.common.repository

import com.rarible.protocol.solana.common.model.TokenOffChainCollection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirst
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.index.Index
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.stereotype.Component

@Component
class TokenOffChainCollectionRepository(
    private val mongo: ReactiveMongoOperations
) {
    suspend fun save(tokenOffChainCollection: TokenOffChainCollection): TokenOffChainCollection =
        mongo.save(tokenOffChainCollection).awaitFirst()

    fun findByOffChainCollectionHash(offChainCollectionHash: String): Flow<TokenOffChainCollection> {
        val criteria = Criteria.where(TokenOffChainCollection::hash.name).isEqualTo(offChainCollectionHash)
        val query = Query(criteria).with(
            Sort.by(
                TokenOffChainCollection::hash.name,
                TokenOffChainCollection::tokenAddress.name,
                "_id"
            )
        )
        return mongo.find(query, TokenOffChainCollection::class.java).asFlow()
    }

    suspend fun createIndexes() {
        val logger = LoggerFactory.getLogger(TokenOffChainCollectionRepository::class.java)
        logger.info("Ensuring indexes on ${TokenOffChainCollection.COLLECTION}")
        MetaIndexes.ALL_INDEXES.forEach { index ->
            mongo.indexOps(TokenOffChainCollection::class.java).ensureIndex(index).awaitFirst()
        }
    }

    private object MetaIndexes {
        val HASH_TOKEN_ADDRESS_ID: Index = Index()
            .on(TokenOffChainCollection::hash.name, Sort.Direction.ASC)
            .on(TokenOffChainCollection::tokenAddress.name, Sort.Direction.ASC)
            .on("_id", Sort.Direction.ASC)

        val ALL_INDEXES = listOf(
            HASH_TOKEN_ADDRESS_ID
        )
    }
}
