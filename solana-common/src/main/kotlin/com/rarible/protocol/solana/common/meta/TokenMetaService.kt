package com.rarible.protocol.solana.common.meta

import com.rarible.protocol.solana.common.model.TokenId
import com.rarible.protocol.solana.common.model.TokenOffChainCollection
import com.rarible.protocol.solana.common.repository.MetaplexMetaRepository
import com.rarible.protocol.solana.common.repository.TokenOffChainCollectionRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class TokenMetaService(
    private val metaplexMetaRepository: MetaplexMetaRepository,
    private val metaMetrics: MetaMetrics,
    private val metaplexOffChainMetadataLoader: MetaplexOffChainMetadataLoader,
    private val tokenOffChainCollectionRepository: TokenOffChainCollectionRepository
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
            logger.error("Failed to load metadata for token $tokenAddress by URL $metadataUrl", e)
            metaMetrics.onMetaLoadingError()
            return null
        }
        val tokenMetaParser = TokenMetaParser(tokenAddress, metadataUrl)
        val offChainMetaJson = try {
            tokenMetaParser.parseOffChainMeta(offChainMetadataJsonContent)
        } catch (e: Exception) {
            logger.error("Failed to parse metadata for token $tokenAddress by URL $metadataUrl", e)
            metaMetrics.onMetaParsingError()
            return null
        }
        val tokenMeta = tokenMetaParser.mergeOnChainAndOffChainMeta(meta, offChainMetaJson)
        val collection = tokenMeta.collection
        if (collection is TokenMeta.Collection.OffChain) {
            // Save the mapping between token and off-chain collection to enable "findTokensByOffChainCollectionHash" API endpoint.
            tokenOffChainCollectionRepository.save(
                TokenOffChainCollection(
                    hash = collection.hash,
                    name = collection.name,
                    family = collection.family,
                    tokenAddress = tokenAddress,
                    metadataUrl = meta.uri
                )
            )
        }
        return tokenMeta
    }

}
