package com.rarible.protocol.solana.common.service

import com.rarible.protocol.solana.common.converter.CollectionConverter
import com.rarible.protocol.solana.common.meta.TokenMetaService
import com.rarible.protocol.solana.common.model.SolanaCollection
import com.rarible.protocol.solana.common.model.SolanaCollectionV1
import com.rarible.protocol.solana.common.model.SolanaCollectionV2
import com.rarible.solana.protocol.dto.CollectionDto
import org.springframework.stereotype.Component

@Component
class CollectionConversionService(
    private val tokenMetaService: TokenMetaService
) {

    // TODO can be optimized for batch
    suspend fun toDto(collection: SolanaCollection): CollectionDto {
        return when (collection) {
            is SolanaCollectionV1 -> CollectionConverter.convertV1(collection)
            is SolanaCollectionV2 -> {
                // Collection should not be stored if there is no meta for it
                val onChainMeta = tokenMetaService.getOnChainMeta(collection.id)
                val offChainMeta = tokenMetaService.getOffChainMeta(collection.id)
                // TODO this should NOT happens if we starting to index from the beginning or with whitelist
                if (onChainMeta == null) {
                    CollectionDto(address = collection.id, name = "Unknown")
                } else {
                    CollectionConverter.convertV2(collection, onChainMeta.metaFields, offChainMeta?.metaFields)
                }
            }
        }
    }
}