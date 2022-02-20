package com.rarible.protocol.solana.common.meta

import com.rarible.protocol.solana.common.model.TokenId
import com.rarible.protocol.solana.common.repository.MetaplexMetaRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class TokenMetaService(
    private val metaplexMetaRepository: MetaplexMetaRepository,
    private val metaMetrics: MetaMetrics,
    private val metaplexOffChainMetadataLoader: MetaplexOffChainMetadataLoader
) {
    private val logger = LoggerFactory.getLogger(TokenMetaService::class.java)

    suspend fun getTokenMeta(tokenAddress: TokenId): TokenMeta? {
        val metaplexMeta = metaplexMetaRepository.findByTokenAddress(tokenAddress) ?: return null
        val meta = metaplexMeta.metaFields
        val metadataUrl = url(meta.uri)
        logger.info("Loading off-chain metadata for token $tokenAddress by URL $metadataUrl")
        val offChainMetadataJsonContent = try {
            metaplexOffChainMetadataLoader.loadOffChainMetadataJson(metadataUrl)
        } catch (e: Exception) {
            metaMetrics.onMetaLoadingError(tokenAddress, meta.uri, e)
            return null
        }
        return try {
            TokenMetaParser(tokenAddress, metadataUrl)
                .parseTokenMetadata(meta, offChainMetadataJsonContent)
        } catch (e: Exception) {
            metaMetrics.onMetaParsingError(tokenAddress, meta.uri, e)
            null
        }
    }

}
