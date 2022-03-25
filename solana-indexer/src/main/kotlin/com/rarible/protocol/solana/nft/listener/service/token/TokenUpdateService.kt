package com.rarible.protocol.solana.nft.listener.service.token

import com.rarible.core.entity.reducer.service.EntityService
import com.rarible.protocol.solana.common.model.Token
import com.rarible.protocol.solana.common.model.TokenId
import com.rarible.protocol.solana.common.repository.TokenRepository
import com.rarible.protocol.solana.common.update.TokenUpdateListener
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class TokenUpdateService(
    private val tokenRepository: TokenRepository,
    private val tokenUpdateListener: TokenUpdateListener
) : EntityService<TokenId, Token> {

    override suspend fun get(id: TokenId): Token? =
        tokenRepository.findByMint(id)

    override suspend fun update(entity: Token): Token {
        if (entity.createdAt == Instant.EPOCH) {
            // Field 'createdAt' has real value only if Initialize event received for the Token
            // if we don't have such init event, balance can't be calculated in right way, so we skip it
            logger.info("Token without Initialize record, skipping it: {}", entity.mint)
            return entity
        }
        val token = tokenRepository.save(entity)
        tokenUpdateListener.onTokenChanged(token)
        logger.info("Updated token: $entity")
        return token
    }

    companion object {
        private val logger = LoggerFactory.getLogger(TokenUpdateService::class.java)
    }
}
