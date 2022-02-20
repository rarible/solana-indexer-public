package com.rarible.protocol.solana.nft.api.service

import com.rarible.protocol.solana.common.model.Token
import com.rarible.protocol.solana.common.repository.TokenRepository
import com.rarible.protocol.solana.nft.api.exceptions.EntityNotFoundApiException
import org.springframework.stereotype.Component

@Component
class TokenService(
    private val tokenRepository: TokenRepository
) {
    suspend fun getToken(tokenAddress: String): Token =
        tokenRepository.findByMint(tokenAddress)
            ?: throw EntityNotFoundApiException("Token", tokenAddress)
}
