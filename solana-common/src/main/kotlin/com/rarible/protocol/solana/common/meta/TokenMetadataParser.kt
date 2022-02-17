package com.rarible.protocol.solana.common.meta

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.rarible.protocol.solana.common.model.MetaplexMetaFields
import com.rarible.protocol.solana.common.model.MetaplexTokenCreator
import com.rarible.protocol.solana.common.model.TokenId
import org.slf4j.LoggerFactory
import java.net.URL

class TokenMetadataParser(
    tokenAddress: TokenId,
    metadataUrl: URL
) {

    private val logger = LoggerFactory.getLogger(TokenMetadataParser::class.java)

    private val jacksonMapper = jacksonObjectMapper()

    private val logPrefix = "Metadata parsing for token $tokenAddress by URL $metadataUrl"

    private fun log(message: String) {
        logger.info("$logPrefix: $message")
    }

    fun parseTokenMetadata(
        meta: MetaplexMetaFields,
        offChainMetadataJsonContent: String
    ): TokenMetadata {
        log("parsing metadata JSON " + offChainMetadataJsonContent.take(1024))
        val offChainMetadataJson =
            jacksonMapper.readValue<MetaplexOffChainMetadataJsonSchema>(offChainMetadataJsonContent)
        return TokenMetadata(
            name = meta.name,
            symbol = meta.symbol,
            description = offChainMetadataJson.description,
            creators = getCreators(meta, offChainMetadataJson),
            collection = getCollection(meta, offChainMetadataJson),
            url = meta.uri,
            attributes = offChainMetadataJson.attributes.orEmpty().mapNotNull { it.getAttribute() },
            contents = parseContents(offChainMetadataJson),
            externalUrl = offChainMetadataJson.external_url
        )
    }

    private fun parseContents(offChainMetadataJson: MetaplexOffChainMetadataJsonSchema) =
        (offChainMetadataJson.parseFiles() + listOfNotNull(
            offChainMetadataJson.parseImage(),
            offChainMetadataJson.parseAnimation()
        )).distinctBy { it.url }

    private fun MetaplexOffChainMetadataJsonSchema.parseImage(): TokenMetadata.Content.ImageContent? =
        if (image == null) null else TokenMetadata.Content.ImageContent(image, null)

    private fun MetaplexOffChainMetadataJsonSchema.parseAnimation(): TokenMetadata.Content.VideoContent? =
        if (animation_url == null) null else TokenMetadata.Content.VideoContent(animation_url, null)

    private fun MetaplexOffChainMetadataJsonSchema.parseFiles(): List<TokenMetadata.Content> =
        properties?.files.orEmpty().mapNotNull { it.parseFile() }

    // TODO[meta]: parse properties.category and other fields.
    private fun MetaplexOffChainMetadataJsonSchema.Properties.File.parseFile(): TokenMetadata.Content? {
        val uri = parseField("file.uri") { this.uri } ?: return null
        val type = parseField("file.type") { this.type }
        return when {
            type == null -> {
                if (listOf("png", "jpg", "jpeg", "svg").any { uri.endsWith(it) }) {
                    TokenMetadata.Content.ImageContent(uri, null)
                } else {
                    TokenMetadata.Content.VideoContent(uri, null)
                }
            }
            type.startsWith("image") -> {
                TokenMetadata.Content.ImageContent(uri, type)
            }
            type.startsWith("video") -> {
                TokenMetadata.Content.VideoContent(uri, type)
            }
            else -> {
                // TODO[meta]: support other types.
                TokenMetadata.Content.VideoContent(uri, type.takeIf { it != "unknown" })
            }
        }
    }

    private fun MetaplexOffChainMetadataJsonSchema.Attribute.getAttribute(): TokenMetadata.Attribute? {
        val traitType = parseField("attribute.trait_type") { this.trait_type } ?: return null
        val value = parseField("attribute.value") { this.value } ?: return null
        return TokenMetadata.Attribute(
            key = traitType,
            value = value,
            type = null,
            format = null
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
            ?: offChainMetadataJson.properties?.creators.orEmpty().map { it.convert() }

    private fun MetaplexOffChainMetadataJsonSchema.Properties.Creator.convert(): MetaplexTokenCreator =
        MetaplexTokenCreator(
            address = address,
            share = share
        )

    private fun <T> parseField(fieldName: String, block: () -> T): T {
        val field = block()
        if (field == null) {
            log("$logPrefix: missing field $fieldName")
        }
        return field
    }
}
