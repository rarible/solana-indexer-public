package com.rarible.protocol.solana.nft.api.service

import com.rarible.protocol.solana.common.meta.TokenMetaService
import com.rarible.protocol.solana.common.model.Token
import com.rarible.protocol.solana.common.model.TokenWithMeta
import com.rarible.protocol.solana.common.repository.MetaplexMetaRepository
import com.rarible.protocol.solana.common.repository.MetaplexOffChainMetaRepository
import com.rarible.protocol.solana.common.repository.TokenRepository
import com.rarible.protocol.solana.nft.api.exceptions.EntityNotFoundApiException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.merge
import org.springframework.stereotype.Component

@Component
class TokenApiService(
    private val tokenRepository: TokenRepository,
    private val tokenMetaService: TokenMetaService,
    private val metaplexMetaRepository: MetaplexMetaRepository,
    private val metaplexOffChainMetaRepository: MetaplexOffChainMetaRepository
) {

    suspend fun getTokensWithMeta(tokenAddresses: List<String>): Flow<TokenWithMeta> {
        val tokens = tokenRepository.findByMints(tokenAddresses)

        return tokens.map { tokenMetaService.extendWithAvailableMeta(it) }
    }

    suspend fun getTokenWithMeta(tokenAddress: String): TokenWithMeta {
        val token = (tokenRepository.findByMint(tokenAddress)
            ?: throw EntityNotFoundApiException("Token", tokenAddress))
        return tokenMetaService.extendWithAvailableMeta(token)
    }

    suspend fun getTokensWithMetaByCollection(collection: String): Flow<TokenWithMeta> {
        val tokensByOnChainCollection = getTokensByMetaplexCollectionAddress(collection)
        val tokensByOffChainCollection = getTokensByOffChainCollectionHash(collection)
        val tokens = merge(tokensByOnChainCollection, tokensByOffChainCollection)

        return tokens.map { tokenMetaService.extendWithAvailableMeta(it) }
    }

    private fun getTokensByMetaplexCollectionAddress(collectionAddress: String): Flow<Token> =
        metaplexMetaRepository.findByCollectionAddress(collectionAddress).mapNotNull { meta ->
            tokenRepository.findByMint(meta.tokenAddress)
        }

    private fun getTokensByOffChainCollectionHash(offChainCollectionHash: String): Flow<Token> =
        metaplexOffChainMetaRepository.findByOffChainCollectionHash(offChainCollectionHash)
            .mapNotNull { tokenOffChainCollection ->
                tokenRepository.findByMint(tokenOffChainCollection.tokenAddress)
            }
}
