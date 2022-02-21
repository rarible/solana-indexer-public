package com.rarible.protocol.solana.nft.listener.service.meta

import com.rarible.core.entity.reducer.service.Reducer
import com.rarible.protocol.solana.common.event.MetaplexCreateMetadataAccountEvent
import com.rarible.protocol.solana.common.event.MetaplexMetaEvent
import com.rarible.protocol.solana.common.event.MetaplexUnVerifyCollectionMetadataEvent
import com.rarible.protocol.solana.common.event.MetaplexUpdateMetadataEvent
import com.rarible.protocol.solana.common.event.MetaplexVerifyCollectionMetadataEvent
import com.rarible.protocol.solana.common.model.MetaplexMeta
import org.springframework.stereotype.Component

@Component
class ForwardMetaplexMetaReducer : Reducer<MetaplexMetaEvent, MetaplexMeta> {
    override suspend fun reduce(entity: MetaplexMeta, event: MetaplexMetaEvent): MetaplexMeta {
        return when (event) {
            is MetaplexCreateMetadataAccountEvent -> entity.copy(
                tokenAddress = event.token,
                metaFields = event.metadata,
                isMutable = event.isMutable
            )
            // TODO[test]: add a test for updating metadata.
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
            // TODO[test]: add a test for UnVerify collection.
            is MetaplexUnVerifyCollectionMetadataEvent -> entity.copy(
                metaFields = entity.metaFields.copy(
                    collection = entity.metaFields.collection?.copy(
                        verified = false
                    )
                )
            )
        }.copy(updatedAt = event.timestamp)
    }
}
