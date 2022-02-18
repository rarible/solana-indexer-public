package com.rarible.protocol.solana.nft.listener.service.meta

import com.rarible.core.application.ApplicationEnvironmentInfo
import com.rarible.core.entity.reducer.service.EventReduceService
import com.rarible.protocol.solana.common.model.EntityEventListeners
import com.rarible.protocol.solana.nft.listener.consumer.EntityEventListener
import com.rarible.protocol.solana.nft.listener.consumer.SolanaLogRecordEvent
import com.rarible.protocol.solana.nft.listener.service.descriptors.SubscriberGroup
import org.springframework.stereotype.Component

@Component
class MetaEventReduceService(
    entityService: MetaUpdateService,
    entityIdService: MetaIdService,
    templateProvider: MetaTemplateProvider,
    reducer: MetaReducer,
    environmentInfo: ApplicationEnvironmentInfo,
    private val metaEventConverter: MetaEventConverter
) : EntityEventListener {
    private val delegate = EventReduceService(entityService, entityIdService, templateProvider, reducer)

    override val id: String = EntityEventListeners.metaHistoryListenerId(environmentInfo.name)

    override val subscriberGroup: SubscriberGroup = SubscriberGroup.METAPLEX_META

    override suspend fun onEntityEvents(events: List<SolanaLogRecordEvent>) {
        val metaEvents = events.flatMap { metaEventConverter.convert(it) }

        delegate.reduceAll(metaEvents)
    }
}
