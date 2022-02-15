package com.rarible.protocol.solana.nft.listener.service.meta

import com.rarible.core.entity.reducer.service.Reducer
import com.rarible.protocol.solana.common.event.MetaplexMetaEvent
import com.rarible.protocol.solana.common.event.MetaplexCreateMetadataEvent
import com.rarible.protocol.solana.common.model.MetaplexMeta
import org.springframework.stereotype.Component

@Component
class ForwardMetaplexMetaReducer : Reducer<MetaplexMetaEvent, MetaplexMeta> {
    override suspend fun reduce(entity: MetaplexMeta, event: MetaplexMetaEvent): MetaplexMeta {
        return when (event) {
            is MetaplexCreateMetadataEvent -> entity.copy(
                meta = event.metadata,
                updatedAt = event.timestamp
            )
        }
    }
}
