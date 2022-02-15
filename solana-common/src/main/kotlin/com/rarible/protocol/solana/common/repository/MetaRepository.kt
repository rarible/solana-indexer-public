package com.rarible.protocol.solana.common.repository

import com.rarible.protocol.solana.common.configuration.RepositoryConfiguration
import com.rarible.protocol.solana.common.model.MetaId
import com.rarible.protocol.solana.common.model.MetaplexMeta
import com.rarible.protocol.solana.common.model.TokenId
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
class MetaRepository(
    private val mongo: ReactiveMongoOperations
) {
    suspend fun save(metaplexMeta: MetaplexMeta): MetaplexMeta =
         mongo.save(metaplexMeta).awaitFirst()

    suspend fun findById(metaAddress: MetaId): MetaplexMeta? =
        mongo.findById<MetaplexMeta>(metaAddress).awaitFirstOrNull()

    suspend fun findByTokenAddress(tokenAddress: TokenId): MetaplexMeta? {
        val criteria = Criteria.where("tokenAddress").isEqualTo(tokenAddress)

        return mongo.find(Query(criteria), MetaplexMeta::class.java).awaitFirstOrNull()
    }

    suspend fun createIndexes() {
        val logger = LoggerFactory.getLogger(MetaRepository::class.java)
        logger.info("Ensuring indexes on ${MetaplexMeta.COLLECTION}")
        MetaIndexes.ALL_INDEXES.forEach { index ->
            mongo.indexOps(MetaplexMeta::class.java).ensureIndex(index).awaitFirst()
        }
    }

    private object MetaIndexes {
        val TOKEN_ADDRESS_ID: Index = Index()
            .on(MetaplexMeta::tokenAddress.name, Sort.Direction.ASC)
            .on("_id", Sort.Direction.ASC)
            .unique()

        val ALL_INDEXES = listOf(
            TOKEN_ADDRESS_ID
        )
    }

}
