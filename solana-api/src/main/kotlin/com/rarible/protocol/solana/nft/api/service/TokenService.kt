package com.rarible.protocol.solana.nft.api.service

import com.rarible.protocol.solana.common.model.Token
import com.rarible.protocol.solana.common.repository.MetaplexMetaRepository
import com.rarible.protocol.solana.common.repository.TokenRepository
import com.rarible.protocol.solana.nft.api.exceptions.EntityNotFoundApiException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import org.springframework.stereotype.Component

@Component
class TokenService(
    private val tokenRepository: TokenRepository,
    private val metaplexMetaRepository: MetaplexMetaRepository
) {
    suspend fun getToken(tokenAddress: String): Token =
        tokenRepository.findByMint(tokenAddress)
            ?: throw EntityNotFoundApiException("Token", tokenAddress)

    suspend fun getTokensByMetaplexCollectionAddress(collectionAddress: String): Flow<Token> =
        metaplexMetaRepository.findByCollectionAddress(collectionAddress).mapNotNull { meta ->
            tokenRepository.findByMint(meta.tokenAddress)
        }
}
