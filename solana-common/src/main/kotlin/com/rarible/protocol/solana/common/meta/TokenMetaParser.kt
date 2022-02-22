package com.rarible.protocol.solana.common.meta

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.rarible.protocol.solana.common.model.MetaplexMetaFields
import com.rarible.protocol.solana.common.model.MetaplexOffChainMetaFields
import com.rarible.protocol.solana.common.model.MetaplexTokenCreator
import org.slf4j.LoggerFactory

object TokenMetaParser {

    private val logger = LoggerFactory.getLogger(TokenMetaParser::class.java)

    private val jacksonMapper = jacksonObjectMapper()

    fun parseMetaplexOffChainMetaFields(offChainMetadataJsonContent: String): MetaplexOffChainMetaFields =
        jacksonMapper
            .readValue<MetaplexOffChainMetadataJsonSchema>(offChainMetadataJsonContent)
            .toMetaplexOffChainMetaFields()

    fun mergeOnChainAndOffChainMeta(
        onChainMeta: MetaplexMetaFields,
        offChainMeta: MetaplexOffChainMetaFields
    ): TokenMeta = TokenMeta(
        name = onChainMeta.name,
        symbol = onChainMeta.symbol,
        url = onChainMeta.uri,
        creators = getCreators(onChainMeta, offChainMeta),
        collection = getCollection(onChainMeta, offChainMeta),
        description = offChainMeta.description,
        attributes = offChainMeta.attributes.mapNotNull { it.getAttribute() },
        contents = parseContents(offChainMeta),
        externalUrl = offChainMeta.externalUrl
    )

    private fun parseContents(offChainMetadataJson: MetaplexOffChainMetaFields) =
        (offChainMetadataJson.parseFiles() + listOfNotNull(
            offChainMetadataJson.parseImage(),
            offChainMetadataJson.parseAnimation()
        )).distinctBy { it.url }

    private fun MetaplexOffChainMetaFields.parseImage(): TokenMeta.Content.ImageContent? =
        if (image == null) null else TokenMeta.Content.ImageContent(image, null)

    private fun MetaplexOffChainMetaFields.parseAnimation(): TokenMeta.Content.VideoContent? =
        if (animationUrl == null) null else TokenMeta.Content.VideoContent(animationUrl, null)

    private fun MetaplexOffChainMetaFields.parseFiles(): List<TokenMeta.Content> =
        properties?.files.orEmpty().mapNotNull { it.parseFile() }

    // TODO[meta]: parse properties.category and other fields.
    private fun MetaplexOffChainMetaFields.Properties.File.parseFile(): TokenMeta.Content? {
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
        offChainMetadataJson: MetaplexOffChainMetaFields
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
        offChainMetadataJson: MetaplexOffChainMetaFields
    ): List<MetaplexTokenCreator> =
        metaplexMeta.creators?.takeIf { it.isNotEmpty() }
            ?: offChainMetadataJson.properties?.creators.orEmpty()

    private fun <T> parseField(fieldName: String, block: () -> T): T {
        val field = block()
        if (field == null) {
            logger.info("Metadata parsing: missing field '$fieldName'")
        }
        return field
    }
}

private fun MetaplexOffChainMetadataJsonSchema.toMetaplexOffChainMetaFields(): MetaplexOffChainMetaFields =
    MetaplexOffChainMetaFields(
        name = name,
        symbol = symbol,
        description = description,
        collection = collection?.let { collection ->
            MetaplexOffChainMetaFields.Collection(
                name = collection.name,
                family = collection.family,
                hash = MetaplexOffChainCollectionHash.calculateCollectionHash(
                    name = collection.name,
                    family = collection.family,
                    creators = properties?.creators.orEmpty().map { it.address }
                )
            )
        },
        sellerFeeBasisPoints = seller_fee_basis_points,
        externalUrl = external_url,
        edition = edition,
        backgroundColor = background_color,
        attributes = attributes.orEmpty().let { attributes ->
            attributes.map {
                MetaplexOffChainMetaFields.Attribute(
                    traitType = it.trait_type,
                    value = it.value
                )
            }
        },
        properties = properties?.let { properties ->
            MetaplexOffChainMetaFields.Properties(
                category = properties.category,
                creators = properties.creators.orEmpty().map {
                    MetaplexTokenCreator(
                        address = it.address,
                        share = it.share
                    )
                },
                files = properties.files?.let { files ->
                    files.map { file ->
                        MetaplexOffChainMetaFields.Properties.File(
                            uri = file.uri,
                            type = file.type
                        )
                    }
                }
            )
        },
        image = image,
        animationUrl = animation_url
    )
