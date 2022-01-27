package com.rarible.protocol.solana.common.repository

import com.rarible.protocol.solana.common.model.Token
import com.rarible.protocol.solana.common.model.TokenId
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.findById
import org.springframework.data.mongodb.core.query
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Component

@Component
class TokenRepository(
    private val mongo: ReactiveMongoOperations
) {
    suspend fun save(token: Token): Token =
        mongo.save(token).awaitFirst()

    suspend fun findById(id: TokenId): Token? =
        mongo.findById<Token>(id).awaitFirstOrNull()

    suspend fun search(query: Query): List<Token> {
        return mongo.query<Token>().matching(query)
            .all()
            .collectList()
            .awaitFirst()
    }
}
