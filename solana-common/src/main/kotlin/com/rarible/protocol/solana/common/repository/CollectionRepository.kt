package com.rarible.protocol.solana.common.repository

import com.rarible.protocol.solana.common.model.SolanaCollection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Component

@Component
class CollectionRepository(
    private val mongo: ReactiveMongoOperations
) {

    val collection = "collection"

    suspend fun save(collection: SolanaCollection): SolanaCollection {
        return mongo.save(collection, this.collection)
            .awaitFirst()
    }

    suspend fun findById(id: String): SolanaCollection? {
        return mongo.findById(id, SolanaCollection::class.java, collection)
            .awaitFirstOrNull()
    }

    fun findAll(fromId: String?, limit: Int): Flow<SolanaCollection> {
        val criteria = fromId?.let { Criteria("_id").gt(it) }
            ?: Criteria()

        val query = Query(criteria)
        query.limit(limit)
        query.with(Sort.by(Sort.Direction.ASC, "_id"))

        return mongo.find(query, SolanaCollection::class.java, collection).asFlow()
    }

}