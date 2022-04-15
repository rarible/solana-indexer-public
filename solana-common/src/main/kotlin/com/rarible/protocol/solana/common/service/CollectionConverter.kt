package com.rarible.protocol.solana.common.service

import com.rarible.protocol.solana.common.converter.TokenMetaConverter
import com.rarible.protocol.solana.common.meta.TokenMeta
import com.rarible.protocol.solana.common.meta.TokenMetaParser
import com.rarible.protocol.solana.common.meta.TokenMetaService
import com.rarible.protocol.solana.common.model.SolanaCollection
import com.rarible.protocol.solana.common.model.SolanaCollectionV1
import com.rarible.protocol.solana.common.model.SolanaCollectionV2
import com.rarible.protocol.solana.dto.CollectionDto
import com.rarible.protocol.solana.dto.CollectionMetaDto
import org.springframework.stereotype.Component

@Component
class CollectionConverter(
    private val tokenMetaService: TokenMetaService
) {

    // TODO can be optimized for batch
    suspend fun toDto(collection: SolanaCollection): CollectionDto {
        return when (collection) {
            is SolanaCollectionV1 -> convertV1(collection)
            is SolanaCollectionV2 -> {
                // Collection should not be stored if there is no meta for it
                val onChainMeta = tokenMetaService.getOnChainMeta(collection.id)
                val offChainMeta = tokenMetaService.getOffChainMeta(collection.id)
                // TODO this should NOT happens if we starting to index from the beginning or with whitelist
                if (onChainMeta == null) {
                    CollectionDto(address = collection.id, name = "Unknown")
                } else {
                    val tokenMeta = TokenMetaParser.mergeOnChainAndOffChainMeta(
                        onChainMeta = onChainMeta.metaFields,
                        offChainMeta = offChainMeta?.metaFields
                    )
                    convertV2(collection, tokenMeta)
                }
            }
        }
    }

    fun convertV1(collection: SolanaCollectionV1): CollectionDto = CollectionDto(
        address = collection.id,
        name = collection.name,
        features = emptyList() // TODO
    )

    fun convertV2(
        collection: SolanaCollectionV2,
        tokenMeta: TokenMeta
    ): CollectionDto = CollectionDto(
        address = collection.id,
        name = tokenMeta.name,
        symbol = tokenMeta.symbol,
        features = emptyList(), // TODO
        creators = tokenMeta.creators.map { it.address },
        meta = CollectionMetaDto(
            name = tokenMeta.name,
            externalLink = tokenMeta.externalUrl,
            sellerFeeBasisPoints = tokenMeta.sellerFeeBasisPoints,
            feeRecipient = null,
            description = tokenMeta.description,
            content = TokenMetaConverter.convert(tokenMeta).content
        )
    )
}