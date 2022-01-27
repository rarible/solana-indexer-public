package com.rarible.protocol.solana.nft.listener.service.token

import com.rarible.core.application.ApplicationEnvironmentInfo
import com.rarible.core.entity.reducer.service.EventReduceService
import com.rarible.protocol.solana.nft.listener.consumer.EntityEventListener
import com.rarible.protocol.solana.nft.listener.consumer.SolanaLogRecordEvent
import com.rarible.protocol.solana.nft.listener.consumer.SubscriberGroup
import com.rarible.protocol.solana.common.model.EntityEventListeners
import org.springframework.stereotype.Component

@Component
class TokenEventReduceService(
    entityService: TokenUpdateService,
    entityIdService: TokenIdService,
    templateProvider: TokenTemplateProvider,
    reducer: TokenReducer,
    environmentInfo: ApplicationEnvironmentInfo,
    private val tokenEventConverter: TokenEventConverter
) : EntityEventListener {
    private val delegate = EventReduceService(entityService, entityIdService, templateProvider, reducer)

    override val id: String = EntityEventListeners.itemHistoryListenerId(environmentInfo.name)

    override val subscriberGroup: SubscriberGroup = "spl-token"

    override suspend fun onEntityEvents(events: List<SolanaLogRecordEvent>) {
        val tokenEvents = events.flatMap { tokenEventConverter.convert(it) }

        delegate.reduceAll(tokenEvents)
    }
}
