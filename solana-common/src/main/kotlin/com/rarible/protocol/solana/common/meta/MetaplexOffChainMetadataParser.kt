package com.rarible.protocol.solana.common.meta

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.rarible.protocol.solana.common.model.MetaplexOffChainMetaFields
import com.rarible.protocol.solana.common.model.MetaplexTokenCreator

object MetaplexOffChainMetadataParser {

    private val jacksonMapper = jacksonObjectMapper()

    fun parseMetaplexOffChainMetaFields(offChainMetadataJsonContent: String): MetaplexOffChainMetaFields =
        jacksonMapper
            .readValue<MetaplexOffChainMetadataJsonSchema>(offChainMetadataJsonContent)
            .toMetaplexOffChainMetaFields()

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

}
