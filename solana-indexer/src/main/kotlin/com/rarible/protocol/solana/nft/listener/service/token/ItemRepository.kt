package com.rarible.protocol.solana.nft.listener.service.token

import com.rarible.protocol.solana.nft.listener.model.Token
import com.rarible.protocol.solana.nft.listener.model.TokenId
import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.findById
import org.springframework.data.mongodb.core.query
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class ItemRepository(
    private val mongo: ReactiveMongoOperations
) {
    fun save(token: Token): Mono<Token> {
        return mongo.save(token)
    }

    fun findById(id: TokenId): Mono<Token> {
        return mongo.findById(id)
    }

    suspend fun search(query: Query): List<Token> {
        return mongo.query<Token>().matching(query)
            .all()
            .collectList()
            .awaitFirst()
    }
}
