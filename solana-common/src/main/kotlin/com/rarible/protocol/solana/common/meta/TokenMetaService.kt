package com.rarible.protocol.solana.common.meta

import com.rarible.core.common.nowMillis
import com.rarible.protocol.solana.common.model.MetaplexMeta
import com.rarible.protocol.solana.common.model.MetaplexOffChainMeta
import com.rarible.protocol.solana.common.model.MetaplexOffChainMetaFields
import com.rarible.protocol.solana.common.model.Token
import com.rarible.protocol.solana.common.model.TokenId
import com.rarible.protocol.solana.common.model.TokenWithMeta
import com.rarible.protocol.solana.common.repository.MetaplexMetaRepository
import com.rarible.protocol.solana.common.repository.MetaplexOffChainMetaRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.net.URL

@Component
class TokenMetaService(
    private val metaplexMetaRepository: MetaplexMetaRepository,
    private val metaMetrics: MetaMetrics,
    private val metaplexOffChainMetadataLoader: MetaplexOffChainMetadataLoader,
    private val metaplexOffChainMetaRepository: MetaplexOffChainMetaRepository
) {
    private val logger = LoggerFactory.getLogger(TokenMetaService::class.java)

    private suspend fun getOnChainMeta(tokenAddress: TokenId): MetaplexMeta? =
        metaplexMetaRepository.findByTokenAddress(tokenAddress)

    private suspend fun getOffChainMeta(tokenAddress: TokenId): MetaplexOffChainMeta? =
        metaplexOffChainMetaRepository.findByTokenAddress(tokenAddress)

    suspend fun extendWithAvailableMeta(token: Token): TokenWithMeta {
        val tokenMeta = getAvailableTokenMeta(token.mint)
        return TokenWithMeta(token, tokenMeta)
    }

    suspend fun getAvailableTokenMeta(tokenAddress: TokenId): TokenMeta? {
        val onChainMeta = getOnChainMeta(tokenAddress) ?: return null
        val offChainMeta = getOffChainMeta(tokenAddress) ?: return null
        return TokenMetaParser.mergeOnChainAndOffChainMeta(
            onChainMeta = onChainMeta.metaFields,
            offChainMeta = offChainMeta.metaFields
        )
    }

    suspend fun loadTokenMeta(tokenAddress: TokenId): TokenMeta? {
        val onChainMeta = getOnChainMeta(tokenAddress) ?: return null
        val metaFields = onChainMeta.metaFields
        val metadataUrl = url(metaFields.uri)
        val offChainMetaFields = loadMetaplexOffChainMetaFields(tokenAddress, metadataUrl) ?: return null
        val metaplexOffChainMeta = MetaplexOffChainMeta(
            tokenAddress = tokenAddress,
            metaFields = offChainMetaFields,
            loadedAt = nowMillis()
        )
        metaplexOffChainMetaRepository.save(metaplexOffChainMeta)
        // TODO: when meta gets changed, we have to send a token update event.
        return TokenMetaParser.mergeOnChainAndOffChainMeta(metaFields, offChainMetaFields)
    }

    suspend fun loadMetaplexOffChainMetaFields(tokenAddress: TokenId, metadataUrl: URL): MetaplexOffChainMetaFields? {
        logger.info("Loading off-chain metadata for token $tokenAddress by URL $metadataUrl")
        val offChainMetadataJsonContent = try {
            metaplexOffChainMetadataLoader.loadOffChainMetadataJson(metadataUrl)
        } catch (e: Exception) {
            logger.error("Failed to load metadata for token $tokenAddress by URL $metadataUrl", e)
            metaMetrics.onMetaLoadingError()
            return null
        }
        return try {
            TokenMetaParser.parseMetaplexOffChainMetaFields(offChainMetadataJsonContent)
        } catch (e: Exception) {
            logger.error("Failed to parse metadata for token $tokenAddress by URL $metadataUrl", e)
            metaMetrics.onMetaParsingError()
            return null
        }
    }

}
