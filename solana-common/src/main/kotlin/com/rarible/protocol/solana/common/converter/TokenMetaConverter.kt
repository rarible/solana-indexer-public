package com.rarible.protocol.solana.common.converter

import com.rarible.protocol.solana.common.meta.TokenMeta
import com.rarible.protocol.solana.common.model.MetaplexTokenCreator
import com.rarible.solana.protocol.dto.CollectionDto
import com.rarible.solana.protocol.dto.ImageContentDto
import com.rarible.solana.protocol.dto.JsonCollectionDto
import com.rarible.solana.protocol.dto.OnChainCollectionDto
import com.rarible.solana.protocol.dto.TokenCreatorPartDto
import com.rarible.solana.protocol.dto.TokenMetaAttributeDto
import com.rarible.solana.protocol.dto.TokenMetaContentDto
import com.rarible.solana.protocol.dto.TokenMetaDto
import com.rarible.solana.protocol.dto.VideoContentDto

object TokenMetaConverter {
    fun convert(tokenMeta: TokenMeta): TokenMetaDto =
        TokenMetaDto(
            name = tokenMeta.name,
            collection = tokenMeta.collection?.let { convert(it) },
            description = tokenMeta.description,
            attributes = tokenMeta.attributes.map { it.convert() },
            content = tokenMeta.contents.map { it.convert() },
            creators = tokenMeta.creators.map { convert(it) }
        )

    private fun TokenMeta.Content.convert(): TokenMetaContentDto =
        when (this) {
            is TokenMeta.Content.ImageContent -> ImageContentDto(
                url = url,
                representation = TokenMetaContentDto.Representation.ORIGINAL,
                mimeType = null,
                size = null,
                width = null,
                height = null
            )
            is TokenMeta.Content.VideoContent -> VideoContentDto(
                url = url,
                representation = TokenMetaContentDto.Representation.ORIGINAL,
                mimeType = null,
                size = null,
                width = null,
                height = null
            )
        }

    fun convert(creator: MetaplexTokenCreator): TokenCreatorPartDto =
        TokenCreatorPartDto(
            address = creator.address,
            share = creator.share
        )

    private fun TokenMeta.Attribute.convert(): TokenMetaAttributeDto =
        TokenMetaAttributeDto(
            type = type,
            value = value,
            format = format,
            key = key
        )

    fun convert(collection: TokenMeta.Collection): CollectionDto =
        when (collection) {
            is TokenMeta.Collection.OffChain -> JsonCollectionDto(
                name = collection.name,
                family = collection.family,
                hash = collection.hash
            )
            is TokenMeta.Collection.OnChain -> OnChainCollectionDto(
                address = collection.address,
                verified = collection.verified
            )
        }
}
