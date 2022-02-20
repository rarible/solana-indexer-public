package com.rarible.protocol.solana.nft.listener.service.meta

import com.rarible.core.entity.reducer.service.Reducer
import com.rarible.protocol.solana.common.event.MetaplexCreateMetadataEvent
import com.rarible.protocol.solana.common.event.MetaplexMetaEvent
import com.rarible.protocol.solana.common.event.MetaplexUpdateMetadataEvent
import com.rarible.protocol.solana.common.event.MetaplexVerifyCollectionMetadataEvent
import com.rarible.protocol.solana.common.model.MetaplexMeta
import org.springframework.stereotype.Component

@Component
class ReversedMetaplexMetaReducer : Reducer<MetaplexMetaEvent, MetaplexMeta> {

    override suspend fun reduce(entity: MetaplexMeta, event: MetaplexMetaEvent): MetaplexMeta {
        return when (event) {
            is MetaplexCreateMetadataEvent -> MetaplexMeta.empty(event.metaAddress)
            is MetaplexVerifyCollectionMetadataEvent -> entity.copy(
                metaFields = entity.metaFields.copy(
                    collection = entity.metaFields.collection?.copy(
                        // TODO: this is not 100% correct: previous must have been 'true'.
                        verified = false
                    )
                )
            )
            is MetaplexUpdateMetadataEvent -> entity // TODO: handle revert to the previous state.
        }
    }
}
