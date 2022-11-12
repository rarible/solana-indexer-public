package com.rarible.protocol.solana.common.meta

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.rarible.protocol.solana.common.model.MetaplexMetaFields
import com.rarible.protocol.solana.common.model.MetaplexOffChainMetaFields
import com.rarible.protocol.solana.common.model.MetaplexTokenCreator

object MetaplexOffChainMetadataParser {

    private val jacksonMapper = jacksonObjectMapper()
    private const val DAPE_CREATOR = "DC2mkgwhy56w3viNtHDjJQmc7SGu2QX785bS4aexojwX"

    fun parseMetaplexOffChainMetaFields(
        offChainMetadataJsonContent: String,
        metaplexMetaFields: MetaplexMetaFields,
    ): MetaplexOffChainMetaFields =
        jacksonMapper
            .readValue<MetaplexOffChainMetadataJsonSchema>(offChainMetadataJsonContent)
            .toMetaplexOffChainMetaFields(metaplexMetaFields)

    private fun MetaplexOffChainMetadataJsonSchema.toMetaplexOffChainMetaFields(metaplexMetaFields: MetaplexMetaFields): MetaplexOffChainMetaFields =
        MetaplexOffChainMetaFields(
            name = name,
            symbol = symbol,
            description = description,
            collection = getCollection(metaplexMetaFields),
            sellerFeeBasisPoints = seller_fee_basis_points,
            externalUrl = external_url,
            edition = edition,
            backgroundColor = background_color,
            attributes = attributes?.let { attributes ->
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
                            share = it.share,
                            verified = false
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

    private fun MetaplexOffChainMetadataJsonSchema.getCollection(
        metaplexMetaFields: MetaplexMetaFields
    ): MetaplexOffChainMetaFields.Collection? {
        return when {
            collection != null ->
                MetaplexOffChainMetaFields.Collection(
                    name = collection.name,
                    family = collection.family,
                    hash = MetaplexOffChainCollectionHash.calculateCollectionHash(
                        name = collection.name,
                        family = collection.family,
                        creators = metaplexMetaFields.creators.map { it.address }
                    )
                )
            properties?.collection != null ->
                MetaplexOffChainMetaFields.Collection(
                    name = properties.collection.name,
                    family = null,
                    hash = MetaplexOffChainCollectionHash.calculateCollectionHash(
                        name = properties.collection.name,
                        family = null,
                        creators = metaplexMetaFields.creators.map { it.address }
                    )
                )
            else -> {
                val collectionAttribute = attributes?.find { it.trait_type == "Collection" }?.value

                if (collectionAttribute != null) {
                    if (metaplexMetaFields.symbol == "DAPE" && metaplexMetaFields.creators.none { it.address == DAPE_CREATOR }) {
                        MetaplexOffChainMetaFields.Collection(
                            name = collectionAttribute,
                            family = null,
                            hash = MetaplexOffChainCollectionHash.calculateCollectionHash(
                                name = collectionAttribute,
                                family = null,
                                creators = metaplexMetaFields.creators.map { it.address }
                            )
                        )
                    } else {
                        MetaplexOffChainMetaFields.Collection(
                            name = collectionAttribute,
                            family = null,
                            hash = MetaplexOffChainCollectionHash.calculateCollectionHash(
                                name = collectionAttribute,
                                family = null,
                                // For such non-standard collections, the creators will probably diverge, too, so we don't count them.
                                creators = emptyList()
                            )
                        )
                    }
                } else {
                    null
                }
            }
        }
    }
}
