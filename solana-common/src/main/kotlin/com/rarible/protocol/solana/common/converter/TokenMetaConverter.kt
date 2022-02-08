package com.rarible.protocol.solana.common.converter

import com.rarible.protocol.solana.common.meta.TokenMetadata
import com.rarible.solana.protocol.dto.TokenMetaCollectionDto
import com.rarible.solana.protocol.dto.TokenMetaDto
import com.rarible.solana.protocol.dto.TokenMetaOffChainCollectionDto
import com.rarible.solana.protocol.dto.TokenMetaOnChainCollectionDto

object TokenMetaConverter {
    fun convert(tokenMeta: TokenMetadata): TokenMetaDto =
        TokenMetaDto(
            name = tokenMeta.name,
            collection = tokenMeta.collection?.convert(),
            description = tokenMeta.description,
            attributes = emptyList(), // TODO[meta]: parse attributes
            content = emptyList() // TODO[meta]: parse content URLs
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
