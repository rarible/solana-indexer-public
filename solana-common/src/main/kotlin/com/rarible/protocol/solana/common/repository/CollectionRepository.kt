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

    private companion object {
        const val ID = "_id"
        const val COLLECTION_NAME = "collection"
    }

    suspend fun save(collection: SolanaCollection): SolanaCollection {
        return mongo.save(collection, COLLECTION_NAME)
            .awaitFirst()
    }

    suspend fun findById(id: String): SolanaCollection? {
        return mongo.findById(id, SolanaCollection::class.java, COLLECTION_NAME)
            .awaitFirstOrNull()
    }

    suspend fun findByIds(ids: List<String>): Flow<SolanaCollection> {
        val criteria = Criteria.where(ID).`in`(ids)
        return mongo.find(Query.query(criteria), SolanaCollection::class.java, COLLECTION_NAME).asFlow()
    }

    fun findAll(fromId: String?): Flow<SolanaCollection> {
        val criteria = fromId?.let { Criteria(ID).gt(it) }
            ?: Criteria()

        val query = Query(criteria)
        query.with(Sort.by(Sort.Direction.ASC, ID))

        return mongo.find(query, SolanaCollection::class.java, COLLECTION_NAME).asFlow()
    }

}