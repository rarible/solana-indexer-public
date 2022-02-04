package com.rarible.protocol.solana.common.meta

import com.rarible.loader.cache.CacheLoaderService
import com.rarible.protocol.solana.common.model.TokenId
import com.rarible.protocol.solana.common.repository.TokenRepository
import org.springframework.stereotype.Component
import java.net.URL

// TODO[meta]: schedule meta loading if not available.
// TODO[meta]: add meta API endpoint.
// TODO[meta]: attach meta to token in API.
@Component
class SolanaMetaService(
    private val tokenRepository: TokenRepository,
    private val solanaMetaCacheLoaderService: CacheLoaderService<SolanaMeta>
) {
    suspend fun getAvailable(tokenAddress: TokenId): SolanaMeta? {
        val token = tokenRepository.findById(tokenAddress) ?: return null
        val metadataUrlString = token.metadataUrl ?: return null

        @Suppress("BlockingMethodInNonBlockingContext")
        val metadataUrl = URL(metadataUrlString)

        val cacheKey = SolanaMetaCacheLoader.encodeKey(
            tokenAddress = tokenAddress,
            metadataUrl = metadataUrl
        )
        return solanaMetaCacheLoaderService.getAvailable(cacheKey)
    }

    suspend fun scheduleLoading(tokenAddress: TokenId, metadataUrl: URL) {
        solanaMetaCacheLoaderService.update(
            SolanaMetaCacheLoader.encodeKey(tokenAddress, metadataUrl)
        )
    }
}
