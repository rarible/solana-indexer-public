package com.rarible.protocol.solana.nft.listener.service.token

import com.rarible.core.entity.reducer.service.EntityService
import com.rarible.protocol.solana.common.model.Token
import com.rarible.protocol.solana.common.model.TokenId
import com.rarible.protocol.solana.common.repository.TokenRepository
import com.rarible.protocol.solana.common.update.TokenUpdateListener
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class TokenUpdateService(
    private val tokenRepository: TokenRepository,
    private val tokenUpdateListener: TokenUpdateListener
) : EntityService<TokenId, Token> {

    override suspend fun get(id: TokenId): Token? =
        tokenRepository.findByMint(id)

    override suspend fun update(entity: Token): Token {
        val token = tokenRepository.save(entity)
        tokenUpdateListener.onTokenChanged(token)
        logger.info("Updated token: $entity")
        return token
    }

    companion object {
        private val logger = LoggerFactory.getLogger(TokenUpdateService::class.java)
    }
}
