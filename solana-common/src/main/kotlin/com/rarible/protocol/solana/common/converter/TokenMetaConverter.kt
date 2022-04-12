package com.rarible.protocol.solana.common.converter

import com.rarible.protocol.solana.common.meta.TokenMeta
import com.rarible.protocol.solana.common.model.MetaplexTokenCreator
import com.rarible.protocol.solana.dto.ImageContentDto
import com.rarible.protocol.solana.dto.TokenCreatorPartDto
import com.rarible.protocol.solana.dto.TokenMetaAttributeDto
import com.rarible.protocol.solana.dto.TokenMetaContentDto
import com.rarible.protocol.solana.dto.TokenMetaDto
import com.rarible.protocol.solana.dto.VideoContentDto

object TokenMetaConverter {

    fun convert(tokenMeta: TokenMeta): TokenMetaDto =
        TokenMetaDto(
            name = tokenMeta.name,
            description = tokenMeta.description,
            attributes = tokenMeta.attributes.orEmpty().map { it.convert() },
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
            share = creator.share * 100 // Count of total 10000
        )

    private fun TokenMeta.Attribute.convert(): TokenMetaAttributeDto =
        TokenMetaAttributeDto(
            type = type,
            value = value,
            format = format,
            key = key
        )

    fun convert(collection: TokenMeta.Collection): String? =
        when (collection) {
            is TokenMeta.Collection.OffChain -> collection.hash
            is TokenMeta.Collection.OnChain -> if (collection.verified) {
                collection.address
            } else {
                null
            }
        }
}
