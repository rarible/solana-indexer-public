package com.rarible.protocol.solana.common.service

import com.rarible.protocol.solana.common.converter.TokenMetaConverter
import com.rarible.protocol.solana.common.meta.TokenMeta
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
    suspend fun toDto(collection: SolanaCollection): CollectionDto? {
        return when (collection) {
            is SolanaCollectionV1 -> convertV1(collection)
            is SolanaCollectionV2 -> {
                val tokenMeta = tokenMetaService.getAvailableTokenMeta(collection.id) ?: return null
                convertV2(collection, tokenMeta)
            }
        }
    }

    private fun convertV1(collection: SolanaCollectionV1): CollectionDto = CollectionDto(
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
        meta = convertCollectionMeta(tokenMeta)
    )

    companion object {
        fun convertCollectionMeta(tokenMeta: TokenMeta) = CollectionMetaDto(
            name = tokenMeta.name,
            externalLink = tokenMeta.externalUrl,
            sellerFeeBasisPoints = tokenMeta.sellerFeeBasisPoints,
            feeRecipient = null,
            description = tokenMeta.description,
            content = TokenMetaConverter.convert(tokenMeta).content
        )
    }
}