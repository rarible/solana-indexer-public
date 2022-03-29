package com.rarible.protocol.solana.nft.api.service

import com.rarible.protocol.solana.common.continuation.DateIdContinuation
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
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class TokenApiService(
    private val tokenRepository: TokenRepository,
    private val tokenMetaService: TokenMetaService,
    private val metaplexMetaRepository: MetaplexMetaRepository,
    private val metaplexOffChainMetaRepository: MetaplexOffChainMetaRepository
) {

    suspend fun findAll(
        lastUpdatedFrom: Instant?,
        lastUpdatedTo: Instant?,
        continuation: DateIdContinuation?, limit: Int
    ): List<TokenWithMeta> {
        val tokens = tokenRepository.findAll(
            lastUpdatedFrom,
            lastUpdatedTo,
            continuation,
            limit
        ).toList()
        return tokens.map { tokenMetaService.extendWithAvailableMeta(it) }
    }

    suspend fun getTokensWithMeta(tokenAddresses: List<String>): Flow<TokenWithMeta> {
        val tokens = tokenRepository.findByMints(tokenAddresses)

        return tokens.map { tokenMetaService.extendWithAvailableMeta(it) }
    }

    suspend fun getTokenWithMeta(tokenAddress: String): TokenWithMeta {
        val token = (tokenRepository.findByMint(tokenAddress)
            ?: throw EntityNotFoundApiException("Token", tokenAddress))
        return tokenMetaService.extendWithAvailableMeta(token)
    }

    suspend fun getTokensWithMetaByCollection(
        collection: String,
        continuation: String?,
        limit: Int
    ): Flow<TokenWithMeta> {
        // TODO it won't work with continuation
        val tokensByOnChainCollection = getTokensByMetaplexCollectionAddress(collection, continuation)
        val tokensByOffChainCollection = getTokensByOffChainCollectionHash(collection, continuation)
        val tokens = merge(tokensByOnChainCollection, tokensByOffChainCollection)

        return tokens.map { tokenMetaService.extendWithAvailableMeta(it) }
    }

    private fun getTokensByMetaplexCollectionAddress(
        collectionAddress: String,
        fromTokenAddress: String?
    ): Flow<Token> {
        return metaplexMetaRepository.findByCollectionAddress(collectionAddress, fromTokenAddress)
            // TODO can be done with batch request
            .mapNotNull { meta ->
                tokenRepository.findByMint(meta.tokenAddress)
            }
    }

    private fun getTokensByOffChainCollectionHash(
        offChainCollectionHash: String,
        fromTokenAddress: String?
    ): Flow<Token> {
        return metaplexOffChainMetaRepository.findByOffChainCollectionHash(offChainCollectionHash, fromTokenAddress)
            .mapNotNull { tokenOffChainCollection ->
                tokenRepository.findByMint(tokenOffChainCollection.tokenAddress)
            }
    }
}
