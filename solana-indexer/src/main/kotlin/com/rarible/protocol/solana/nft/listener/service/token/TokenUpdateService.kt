package com.rarible.protocol.solana.nft.listener.service.token

import com.rarible.core.entity.reducer.service.EntityService
import com.rarible.protocol.solana.nft.listener.model.Token
import com.rarible.protocol.solana.nft.listener.model.TokenId
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class TokenUpdateService(
    private val tokenRepository: TokenRepository,
) : EntityService<TokenId, Token> {

    override suspend fun get(id: TokenId): Token? {
        return tokenRepository.findById(id).awaitFirstOrNull()
    }

    override suspend fun update(entity: Token): Token {
        val savedItem = tokenRepository.save(entity).awaitFirst()

        logger.info("Updated item: $entity")
        return savedItem
    }

    companion object {
        private val logger = LoggerFactory.getLogger(TokenUpdateService::class.java)
    }
}
