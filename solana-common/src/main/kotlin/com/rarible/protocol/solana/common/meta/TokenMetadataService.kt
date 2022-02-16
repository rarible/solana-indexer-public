package com.rarible.protocol.solana.common.meta

import com.rarible.protocol.solana.common.model.MetaplexMetaFields
import com.rarible.protocol.solana.common.model.MetaplexTokenCreator
import com.rarible.protocol.solana.common.model.TokenId
import com.rarible.protocol.solana.common.repository.MetaRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class TokenMetadataService(
    private val metaRepository: MetaRepository,
    private val metaplexOffChainMetadataLoader: MetaplexOffChainMetadataLoader
) {
    private val logger = LoggerFactory.getLogger(TokenMetadataService::class.java)

    suspend fun getTokenMetadata(tokenAddress: TokenId): TokenMetadata? {
        val metaplexMeta = metaRepository.findByTokenAddress(tokenAddress) ?: return null
        val meta = metaplexMeta.metaFields
        val metadataUrl = url(meta.uri)
        logger.info("Loading off-chain metadata for token $tokenAddress by URL $metadataUrl")
        val offChainMetadataJson = metaplexOffChainMetadataLoader.loadOffChainMetadataJson(metadataUrl)
        return TokenMetadata(
            name = meta.name,
            symbol = meta.symbol,
            description = offChainMetadataJson.description,
            creators = getCreators(meta, offChainMetadataJson),
            collection = getCollection(meta, offChainMetadataJson),
            url = meta.uri
        )
    }

    private fun getCollection(
        metaplexMeta: MetaplexMetaFields,
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
        metaplexMeta: MetaplexMetaFields,
        offChainMetadataJson: MetaplexOffChainMetadataJsonSchema
    ): List<MetaplexTokenCreator> =
        metaplexMeta.creators?.takeIf { it.isNotEmpty() }
            ?: offChainMetadataJson.properties.creators.map { it.convert() }

    private fun MetaplexOffChainMetadataJsonSchema.Properties.Creator.convert(): MetaplexTokenCreator = MetaplexTokenCreator(
        address = address,
        share = share
    )

}
