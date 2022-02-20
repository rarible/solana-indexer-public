package com.rarible.protocol.solana.nft.api.service

import com.rarible.protocol.solana.common.model.Token
import com.rarible.protocol.solana.common.repository.MetaRepository
import com.rarible.protocol.solana.common.repository.TokenRepository
import com.rarible.protocol.solana.nft.api.exceptions.EntityNotFoundApiException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import org.springframework.stereotype.Component

@Component
class TokenService(
    private val tokenRepository: TokenRepository,
    private val metaRepository: MetaRepository
) {
    suspend fun getToken(tokenAddress: String): Token =
        tokenRepository.findByMint(tokenAddress)
            ?: throw EntityNotFoundApiException("Token", tokenAddress)

    suspend fun getTokensByMetaplexCollectionAddress(collectionAddress: String): Flow<Token> =
        metaRepository.findByCollectionAddress(collectionAddress).mapNotNull { meta ->
            tokenRepository.findByMint(meta.tokenAddress)
        }
}
