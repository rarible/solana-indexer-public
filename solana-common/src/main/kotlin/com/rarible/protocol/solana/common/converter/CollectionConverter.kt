package com.rarible.protocol.solana.common.converter

import com.rarible.protocol.solana.common.meta.TokenMetaParser
import com.rarible.protocol.solana.common.model.MetaplexMetaFields
import com.rarible.protocol.solana.common.model.MetaplexOffChainMetaFields
import com.rarible.protocol.solana.common.model.SolanaCollectionV1
import com.rarible.protocol.solana.common.model.SolanaCollectionV2
import com.rarible.protocol.solana.dto.CollectionDto
import com.rarible.protocol.solana.dto.CollectionMetaDto

object CollectionConverter {

    fun convertV1(collection: SolanaCollectionV1): CollectionDto {
        return CollectionDto(
            address = collection.id,
            name = collection.name,
            features = emptyList() // TODO
        )
    }

    fun convertV2(
        collection: SolanaCollectionV2,
        tokenMetaFields: MetaplexMetaFields,
        tokenOffChainMetaFields: MetaplexOffChainMetaFields?
    ): CollectionDto {
        return CollectionDto(
            address = collection.id,
            name = tokenMetaFields.name,
            symbol = tokenMetaFields.symbol,
            features = emptyList(), // TODO
            creators = tokenMetaFields.creators?.map { it.address } ?: emptyList(),
            meta = CollectionMetaDto(
                name = tokenMetaFields.name,
                externalLink = tokenOffChainMetaFields?.externalUrl,
                sellerFeeBasisPoints = tokenOffChainMetaFields?.sellerFeeBasisPoints,
                feeRecipient = null,
                description = tokenOffChainMetaFields?.description,
                content = tokenOffChainMetaFields?.let {
                    val tokenMeta = TokenMetaParser.mergeOnChainAndOffChainMeta(
                        tokenMetaFields, tokenOffChainMetaFields
                    )
                    TokenMetaConverter.convert(tokenMeta).content
                } ?: emptyList()
            )
        )
    }
}

