package com.rarible.protocol.solana.common.meta

import com.rarible.protocol.solana.common.model.MetaplexTokenMeta
import com.rarible.protocol.solana.common.model.MetaplexTokenCreator
import com.rarible.protocol.solana.common.model.TokenId
import com.rarible.protocol.solana.common.repository.TokenRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class TokenMetadataService(
    private val tokenRepository: TokenRepository,
    private val metaplexOffChainMetadataLoader: MetaplexOffChainMetadataLoader
) {
    private val logger = LoggerFactory.getLogger(TokenMetadataService::class.java)

    suspend fun getTokenMetadata(tokenAddress: TokenId): TokenMetadata? {
        val token = tokenRepository.findById(tokenAddress) ?: return null
        val metaplexMeta = token.metaplexMeta
        val metadataUrl = metaplexMeta?.uri?.let { url(it) } ?: return null
        logger.info("Loading off-chain metadata for token $tokenAddress by URL $metadataUrl")
        val offChainMetadataJson = metaplexOffChainMetadataLoader.loadOffChainMetadataJson(metadataUrl)
        return TokenMetadata(
            name = metaplexMeta.name,
            symbol = metaplexMeta.symbol,
            description = offChainMetadataJson.description,
            creators = getCreators(metaplexMeta, offChainMetadataJson),
            collection = getCollection(metaplexMeta, offChainMetadataJson),
            url = metaplexMeta.uri
        )
    }

    private fun getCollection(
        metaplexMeta: MetaplexTokenMeta,
        offChainMetadataJson: MetaplexOffChainMetadataJsonSchema
    ): TokenMetadata.Collection? {
        val onChainCollection = metaplexMeta.collection
        if (onChainCollection != null) {
            return TokenMetadata.Collection.OnChain(
                address = onChainCollection.address,
                verified = onChainCollection.verified
            )
        }
        val offChainCollection = offChainMetadataJson.collection ?: return null
        return TokenMetadata.Collection.OffChain(
            name = offChainCollection.name,
            family = offChainCollection.family,
            hash = MetaplexOffChainCollectionHash.calculateCollectionHash(
                name = offChainCollection.name,
                family = offChainCollection.family,
                creators = metaplexMeta.creators.orEmpty().map { it.address }
            )
        )
    }

    private fun getCreators(
        metaplexMeta: MetaplexTokenMeta,
        offChainMetadataJson: MetaplexOffChainMetadataJsonSchema
    ): List<MetaplexTokenCreator> =
        metaplexMeta.creators?.takeIf { it.isNotEmpty() }
            ?: offChainMetadataJson.properties.creators.map { it.convert() }

    private fun MetaplexOffChainMetadataJsonSchema.Properties.Creator.convert(): MetaplexTokenCreator = MetaplexTokenCreator(
        address = address,
        share = share
    )

}
