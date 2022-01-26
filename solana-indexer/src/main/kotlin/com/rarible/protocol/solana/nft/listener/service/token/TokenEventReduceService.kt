package com.rarible.protocol.solana.nft.listener.service.token

import com.rarible.core.application.ApplicationEnvironmentInfo
import com.rarible.core.entity.reducer.service.EventReduceService
import com.rarible.protocol.solana.nft.listener.consumer.EntityEventListener
import com.rarible.protocol.solana.nft.listener.consumer.SolanaLogRecordEvent
import com.rarible.protocol.solana.nft.listener.consumer.SubscriberGroup
import com.rarible.protocol.solana.nft.listener.model.EntityEventListeners
import org.springframework.stereotype.Component

@Component
class TokenEventReduceService(
    entityService: ItemUpdateService,
    entityIdService: TokenIdService,
    templateProvider: TokenTemplateProvider,
    reducer: TokenReducer,
    environmentInfo: ApplicationEnvironmentInfo
) : EntityEventListener {
    private val delegate = EventReduceService(entityService, entityIdService, templateProvider, reducer)

    override val id: String = EntityEventListeners.itemHistoryListenerId(environmentInfo.name)

    override val subscriberGroup: SubscriberGroup = "spl-token"

    override suspend fun onEntityEvents(events: List<SolanaLogRecordEvent>) {
        delegate.reduceAll(events)
    }
}
