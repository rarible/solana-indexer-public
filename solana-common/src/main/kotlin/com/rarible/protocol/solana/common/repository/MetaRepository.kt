package com.rarible.protocol.solana.common.repository

import com.rarible.protocol.solana.common.model.MetaId
import com.rarible.protocol.solana.common.model.MetaplexMeta
import com.rarible.protocol.solana.common.model.TokenId
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.find
import org.springframework.data.mongodb.core.findById
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.stereotype.Component

@Component
// TODO add index tokenAddress
class MetaRepository(
    private val mongo: ReactiveMongoOperations
) {
    suspend fun save(token: MetaplexMeta): MetaplexMeta =
        mongo.save(token).awaitFirst()

    suspend fun findById(metaAddress: MetaId): MetaplexMeta? =
        mongo.findById<MetaplexMeta>(metaAddress).awaitFirstOrNull()

    suspend fun findByTokenAddress(tokenAddress: TokenId): MetaplexMeta? {
        val criteria = Criteria.where("tokenAddress").isEqualTo(tokenAddress)

        return mongo.find(Query(criteria), MetaplexMeta::class.java).awaitFirst()
    }
}
