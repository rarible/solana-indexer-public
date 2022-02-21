package com.rarible.protocol.solana.nft.listener.service.meta

import com.rarible.core.entity.reducer.service.Reducer
import com.rarible.protocol.solana.common.event.MetaplexCreateMetadataEvent
import com.rarible.protocol.solana.common.event.MetaplexMetaEvent
import com.rarible.protocol.solana.common.event.MetaplexUpdateMetadataEvent
import com.rarible.protocol.solana.common.event.MetaplexVerifyCollectionMetadataEvent
import com.rarible.protocol.solana.common.model.MetaplexMeta
import org.springframework.stereotype.Component

@Component
class ForwardMetaplexMetaReducer : Reducer<MetaplexMetaEvent, MetaplexMeta> {
    override suspend fun reduce(entity: MetaplexMeta, event: MetaplexMetaEvent): MetaplexMeta {
        return when (event) {
            is MetaplexCreateMetadataEvent -> entity.copy(
                tokenAddress = event.token,
                metaFields = event.metadata,
                isMutable = event.isMutable
            )
            // TODO: add a test for updating metadata.
            is MetaplexUpdateMetadataEvent -> entity.copy(
                metaFields = event.newMetadata ?: entity.metaFields,
                isMutable = event.newIsMutable ?: entity.isMutable
            )
            is MetaplexVerifyCollectionMetadataEvent -> entity.copy(
                metaFields = entity.metaFields.copy(
                    collection = entity.metaFields.collection?.copy(
                        verified = true
                    )
                )
            )
        }.copy(updatedAt = event.timestamp)
    }
}
