package com.rarible.protocol.solana.nft.listener.service.meta

import com.rarible.core.entity.reducer.chain.combineIntoChain
import com.rarible.core.entity.reducer.service.Reducer
import com.rarible.protocol.solana.common.event.MetaplexMetaEvent
import com.rarible.protocol.solana.common.model.MetaplexMeta
import com.rarible.protocol.solana.nft.listener.service.LoggingReducer
import org.springframework.stereotype.Component

@Component
class MetaReducer(
    eventStatusTokenReducer: EventStatusMetaReducer,
    tokenMetricReducer: MetaplexMetaMetricReducer
) : Reducer<MetaplexMetaEvent, MetaplexMeta> {

    private val eventStatusTokenReducer = combineIntoChain(
        LoggingReducer(),
        tokenMetricReducer,
        eventStatusTokenReducer
    )

    override suspend fun reduce(entity: MetaplexMeta, event: MetaplexMetaEvent): MetaplexMeta {
        return eventStatusTokenReducer.reduce(entity, event)
    }
}