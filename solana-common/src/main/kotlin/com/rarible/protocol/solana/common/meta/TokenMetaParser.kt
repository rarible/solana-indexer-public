package com.rarible.protocol.solana.common.meta

import com.rarible.protocol.solana.common.model.MetaplexMetaFields
import com.rarible.protocol.solana.common.model.MetaplexOffChainMetaFields
import org.slf4j.LoggerFactory

object TokenMetaParser {

    private val logger = LoggerFactory.getLogger(TokenMetaParser::class.java)

    fun mergeOnChainAndOffChainMeta(
        onChainMeta: MetaplexMetaFields,
        offChainMeta: MetaplexOffChainMetaFields?
    ): TokenMeta = TokenMeta(
        name = onChainMeta.name,
        symbol = onChainMeta.symbol,
        url = onChainMeta.uri,
        creators = onChainMeta.creators,
        collection = getCollection(onChainMeta, offChainMeta),
        description = offChainMeta?.description,
        attributes = offChainMeta?.attributes?.mapNotNull { it.getAttribute() },
        contents = offChainMeta?.let { parseContents(it) } ?: emptyList(),
        externalUrl = offChainMeta?.externalUrl
    )

    private fun parseContents(metaplexOffChainMetaFields: MetaplexOffChainMetaFields): List<TokenMeta.Content> =
        (metaplexOffChainMetaFields.getFiles() + listOfNotNull(
            metaplexOffChainMetaFields.getImage(),
            metaplexOffChainMetaFields.getAnimation()
        )).distinctBy { it.url }

    private fun MetaplexOffChainMetaFields.getImage(): TokenMeta.Content.ImageContent? =
        if (image == null) null else TokenMeta.Content.ImageContent(image, null)

    private fun MetaplexOffChainMetaFields.getAnimation(): TokenMeta.Content.VideoContent? =
        if (animationUrl == null) null else TokenMeta.Content.VideoContent(animationUrl, null)

    private fun MetaplexOffChainMetaFields.getFiles(): List<TokenMeta.Content> =
        properties?.files.orEmpty().mapNotNull { it.getFile() }

    // TODO[meta]: parse properties.category and other fields.
    private fun MetaplexOffChainMetaFields.Properties.File.getFile(): TokenMeta.Content? {
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

    private fun MetaplexOffChainMetaFields.Attribute.getAttribute(): TokenMeta.Attribute? {
        val traitType = parseField("attribute.trait_type") { this.traitType } ?: return null
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
        metaplexOffChainMetaFields: MetaplexOffChainMetaFields?
    ): TokenMeta.Collection? {
        val onChainCollection = metaplexMeta.collection
        if (onChainCollection != null) {
            return TokenMeta.Collection.OnChain(
                address = onChainCollection.address,
                verified = onChainCollection.verified
            )
        }
        val offChainCollection = metaplexOffChainMetaFields?.collection ?: return null
        return TokenMeta.Collection.OffChain(
            name = offChainCollection.name,
            family = offChainCollection.family,
            hash = offChainCollection.hash
        )
    }

    private fun <T> parseField(fieldName: String, block: () -> T): T {
        val field = block()
        if (field == null) {
            logger.info("Metadata parsing: missing field '$fieldName'")
        }
        return field
    }
}
