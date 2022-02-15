package com.rarible.protocol.solana.nft.listener.service.meta

import com.rarible.core.entity.reducer.service.Reducer
import com.rarible.protocol.solana.common.event.MetaplexMetaEvent
import com.rarible.protocol.solana.common.model.MetaplexMeta
import org.springframework.stereotype.Component

@Component
class EventStatusMetaReducer(
    private val forwardChainTokenReducer: ForwardMetaplexMetaReducer,
    private val reversedChainTokenReducer: ReversedMetaplexMetaReducer,
) : Reducer<MetaplexMetaEvent, MetaplexMeta> {
    override suspend fun reduce(entity: MetaplexMeta, event: MetaplexMetaEvent): MetaplexMeta {
        return if (event.reversed) {
            reversedChainTokenReducer.reduce(entity, event)
        } else {
            forwardChainTokenReducer.reduce(entity, event)
        }
    }
}