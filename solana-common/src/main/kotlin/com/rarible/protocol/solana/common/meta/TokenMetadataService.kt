package com.rarible.protocol.solana.common.meta

import com.rarible.protocol.solana.common.model.TokenId
import com.rarible.protocol.solana.common.repository.MetaRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class TokenMetadataService(
    private val metaRepository: MetaRepository,
    private val metaMetrics: MetaMetrics,
    private val metaplexOffChainMetadataLoader: MetaplexOffChainMetadataLoader
) {
    private val logger = LoggerFactory.getLogger(TokenMetadataService::class.java)

    suspend fun getTokenMetadata(tokenAddress: TokenId): TokenMetadata? {
        val metaplexMeta = metaRepository.findByTokenAddress(tokenAddress) ?: return null
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
            TokenMetadataParser(tokenAddress, metadataUrl)
                .parseTokenMetadata(meta, offChainMetadataJsonContent)
        } catch (e: Exception) {
            metaMetrics.onMetaParsingError(tokenAddress, meta.uri, e)
            null
        }
    }

}
