package com.rarible.protocol.solana.common.repository

import com.rarible.protocol.solana.common.model.MetaId
import com.rarible.protocol.solana.common.model.MetaplexMeta
import com.rarible.protocol.solana.common.model.MetaplexMetaFields
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

@Component
class MetaRepository(
    private val mongo: ReactiveMongoOperations
) {
    suspend fun save(metaplexMeta: MetaplexMeta): MetaplexMeta =
         mongo.save(metaplexMeta).awaitFirst()

    suspend fun findByMetaAddress(metaAddress: MetaId): MetaplexMeta? =
        mongo.findById<MetaplexMeta>(metaAddress).awaitFirstOrNull()

    suspend fun findByTokenAddress(tokenAddress: TokenId): MetaplexMeta? {
        val criteria = Criteria.where(MetaplexMeta::tokenAddress.name).isEqualTo(tokenAddress)
        return mongo.find(Query(criteria), MetaplexMeta::class.java).awaitFirstOrNull()
    }

    fun findByCollectionAddress(collectionAddress: String): Flow<MetaplexMeta> {
        val criteria = Criteria.where(collectionAddressKey).isEqualTo(collectionAddress)
        val query = Query(criteria).with(Sort.by(collectionAddressKey, MetaplexMeta::tokenAddress.name, "_id"))
        return mongo.find(query, MetaplexMeta::class.java).asFlow()
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

        val COLLECTION_ADDRESS_TOKEN_ADDRESS_ID: Index = Index()
            .on(collectionAddressKey, Sort.Direction.ASC)
            .on(MetaplexMeta::tokenAddress.name, Sort.Direction.ASC)
            .on("_id", Sort.Direction.ASC)
            .sparse()

        val ALL_INDEXES = listOf(
            TOKEN_ADDRESS_ID,
            COLLECTION_ADDRESS_TOKEN_ADDRESS_ID
        )
    }

    companion object {
        val collectionAddressKey = MetaplexMeta::metaFields.name + "." + MetaplexMetaFields::collection.name + "." + MetaplexMetaFields.Collection::address.name
    }

}
