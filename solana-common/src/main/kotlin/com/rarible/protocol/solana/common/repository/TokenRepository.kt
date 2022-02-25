package com.rarible.protocol.solana.common.repository

import com.rarible.protocol.solana.common.model.Token
import com.rarible.protocol.solana.common.model.TokenId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.findById
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Component

@Component
class TokenRepository(
    private val mongo: ReactiveMongoOperations
) {
    suspend fun save(token: Token): Token =
        mongo.save(token).awaitFirst()

    suspend fun findByMint(mint: TokenId): Token? =
        mongo.findById<Token>(mint).awaitFirstOrNull()

    suspend fun findByMints(mints: List<TokenId>): Flow<Token> =
        mongo.find(Query(Criteria.where("_id").`in`(mints)).with(Sort.by("_id")), Token::class.java).asFlow()
}
