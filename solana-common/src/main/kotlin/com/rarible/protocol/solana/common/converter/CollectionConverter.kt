package com.rarible.protocol.solana.common.converter

import com.rarible.protocol.solana.common.model.Collection
import com.rarible.solana.protocol.dto.CollectionDto
import com.rarible.solana.protocol.dto.JsonCollectionDto
import com.rarible.solana.protocol.dto.OnChainCollectionDto

object CollectionConverter {
    fun convert(collection: Collection): CollectionDto =
        when (collection) {
            is Collection.JsonCollection -> JsonCollectionDto(
                name = collection.name,
                family = collection.family,
                hash = collection.hash
            )
            is Collection.OnChainCollection -> OnChainCollectionDto(
                address = collection.address,
                verified = collection.verified
            )
        }
}
