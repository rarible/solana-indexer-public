package com.rarible.protocol.solana.common.meta

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.rarible.protocol.solana.common.model.MetaplexMetaFields
import com.rarible.protocol.solana.common.model.MetaplexTokenCreator
import com.rarible.protocol.solana.common.model.TokenId
import org.slf4j.LoggerFactory
import java.net.URL

class TokenMetaParser(
    tokenAddress: TokenId,
    metadataUrl: URL
) {

    private val logger = LoggerFactory.getLogger(TokenMetaParser::class.java)

    private val jacksonMapper = jacksonObjectMapper()

    private val logPrefix = "Metadata parsing for token $tokenAddress by URL $metadataUrl"

    private fun log(message: String) {
        logger.info("$logPrefix: $message")
    }

    fun parseOffChainMeta(offChainMetadataJsonContent: String): MetaplexOffChainMetadataJsonSchema =
        jacksonMapper.readValue(offChainMetadataJsonContent)

    fun mergeOnChainAndOffChainMeta(
        onChainMeta: MetaplexMetaFields,
        offChainMeta: MetaplexOffChainMetadataJsonSchema
    ): TokenMeta = TokenMeta(
        name = onChainMeta.name,
        symbol = onChainMeta.symbol,
        description = offChainMeta.description,
        creators = getCreators(onChainMeta, offChainMeta),
        collection = getCollection(onChainMeta, offChainMeta),
        url = onChainMeta.uri,
        attributes = offChainMeta.attributes.orEmpty().mapNotNull { it.getAttribute() },
        contents = parseContents(offChainMeta),
        externalUrl = offChainMeta.external_url
    )

    private fun parseContents(offChainMetadataJson: MetaplexOffChainMetadataJsonSchema) =
        (offChainMetadataJson.parseFiles() + listOfNotNull(
            offChainMetadataJson.parseImage(),
            offChainMetadataJson.parseAnimation()
        )).distinctBy { it.url }

    private fun MetaplexOffChainMetadataJsonSchema.parseImage(): TokenMeta.Content.ImageContent? =
        if (image == null) null else TokenMeta.Content.ImageContent(image, null)

    private fun MetaplexOffChainMetadataJsonSchema.parseAnimation(): TokenMeta.Content.VideoContent? =
        if (animation_url == null) null else TokenMeta.Content.VideoContent(animation_url, null)

    private fun MetaplexOffChainMetadataJsonSchema.parseFiles(): List<TokenMeta.Content> =
        properties?.files.orEmpty().mapNotNull { it.parseFile() }

    // TODO[meta]: parse properties.category and other fields.
    private fun MetaplexOffChainMetadataJsonSchema.Properties.File.parseFile(): TokenMeta.Content? {
        val uri = parseField("file.uri") { this.uri } ?: return null
        val type = parseField("file.type") { this.type }
        return when {
            type == null -> {
                if (listOf("png", "jpg", "jpeg", "svg").any { uri.endsWith(it) }) {
                    TokenMeta.Content.ImageContent(uri, null)
                } else {
                    TokenMeta.Content.VideoContent(uri, null)
                }
            }
            type.startsWith("image") -> {
                TokenMeta.Content.ImageContent(uri, type)
            }
            type.startsWith("video") -> {
                TokenMeta.Content.VideoContent(uri, type)
            }
            else -> {
                // TODO[meta]: support other types.
                TokenMeta.Content.VideoContent(uri, type.takeIf { it != "unknown" })
            }
        }
    }

    private fun MetaplexOffChainMetadataJsonSchema.Attribute.getAttribute(): TokenMeta.Attribute? {
        val traitType = parseField("attribute.trait_type") { this.trait_type } ?: return null
        val value = parseField("attribute.value") { this.value } ?: return null
        return TokenMeta.Attribute(
            key = traitType,
            value = value,
            type = null,
            format = null
        )
    }

    private fun getCollection(
        metaplexMeta: MetaplexMetaFields,
        offChainMetadataJson: MetaplexOffChainMetadataJsonSchema
    ): TokenMeta.Collection? {
        val onChainCollection = metaplexMeta.collection
        if (onChainCollection != null) {
            return TokenMeta.Collection.OnChain(
                address = onChainCollection.address,
                verified = onChainCollection.verified
            )
        }
        val offChainCollection = offChainMetadataJson.collection ?: return null
        return TokenMeta.Collection.OffChain(
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
