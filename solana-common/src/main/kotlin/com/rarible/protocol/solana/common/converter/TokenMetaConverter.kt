package com.rarible.protocol.solana.common.converter

import com.rarible.protocol.solana.common.meta.TokenMetadata
import com.rarible.solana.protocol.dto.ImageContentDto
import com.rarible.solana.protocol.dto.TokenMetaAttributeDto
import com.rarible.solana.protocol.dto.TokenMetaCollectionDto
import com.rarible.solana.protocol.dto.TokenMetaContentDto
import com.rarible.solana.protocol.dto.TokenMetaDto
import com.rarible.solana.protocol.dto.TokenMetaOffChainCollectionDto
import com.rarible.solana.protocol.dto.TokenMetaOnChainCollectionDto
import com.rarible.solana.protocol.dto.VideoContentDto

object TokenMetaConverter {
    fun convert(tokenMeta: TokenMetadata): TokenMetaDto =
        TokenMetaDto(
            name = tokenMeta.name,
            collection = tokenMeta.collection?.convert(),
            description = tokenMeta.description,
            attributes = tokenMeta.attributes.map { it.convert() },
            content = tokenMeta.contents.map { it.convert() }
        )

    private fun TokenMetadata.Content.convert(): TokenMetaContentDto =
        when (this) {
            is TokenMetadata.Content.ImageContent -> ImageContentDto(
                url = url,
                representation = TokenMetaContentDto.Representation.ORIGINAL,
                mimeType = null,
                size = null,
                width = null,
                height = null
            )
            is TokenMetadata.Content.VideoContent -> VideoContentDto(
                url = url,
                representation = TokenMetaContentDto.Representation.ORIGINAL,
                mimeType = null,
                size = null,
                width = null,
                height = null
            )
        }

    private fun TokenMetadata.Attribute.convert(): TokenMetaAttributeDto =
        TokenMetaAttributeDto(
            type = type,
            value = value,
            format = format,
            key = key
        )

    private fun TokenMetadata.Collection.convert(): TokenMetaCollectionDto =
        when (this) {
            is TokenMetadata.Collection.OffChain -> TokenMetaOffChainCollectionDto(
                name = name,
                family = family,
                hash = hash
            )
            is TokenMetadata.Collection.OnChain -> TokenMetaOnChainCollectionDto(
                address = address,
                verified = verified
            )
        }
}
