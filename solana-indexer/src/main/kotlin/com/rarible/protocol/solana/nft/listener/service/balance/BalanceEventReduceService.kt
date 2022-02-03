package com.rarible.protocol.solana.nft.listener.service.balance

import com.rarible.core.application.ApplicationEnvironmentInfo
import com.rarible.core.entity.reducer.service.EventReduceService
import com.rarible.protocol.solana.nft.listener.consumer.EntityEventListener
import com.rarible.protocol.solana.nft.listener.consumer.SolanaLogRecordEvent
import com.rarible.protocol.solana.nft.listener.consumer.SubscriberGroup
import com.rarible.protocol.solana.common.model.EntityEventListeners
import com.rarible.protocol.solana.nft.listener.service.descriptors.SubscriberGroups
import org.springframework.stereotype.Component

@Component
class BalanceEventReduceService(
    entityService: BalanceUpdateService,
    entityIdService: BalanceIdService,
    templateProvider: BalanceTemplateProvider,
    reducer: BalanceReducer,
    environmentInfo: ApplicationEnvironmentInfo,
    val converter: BalanceEventConverter
) : EntityEventListener {
    private val delegate = EventReduceService(entityService, entityIdService, templateProvider, reducer)

    override val id: String = EntityEventListeners.balanceHistoryListenerId(environmentInfo.name)

    override val subscriberGroup: SubscriberGroup = SubscriberGroups.SPL_TOKEN

    suspend fun reduce(events: List<SolanaLogRecordEvent>) {
        val balanceEvents = events.flatMap { converter.convert(it) }

        delegate.reduceAll(balanceEvents)
    }

    override suspend fun onEntityEvents(events: List<SolanaLogRecordEvent>) {
        val balanceEvents = events.flatMap { converter.convert(it) }

        delegate.reduceAll(balanceEvents)
    }
}
