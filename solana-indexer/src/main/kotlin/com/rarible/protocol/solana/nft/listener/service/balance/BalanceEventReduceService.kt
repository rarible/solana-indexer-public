package com.rarible.protocol.solana.nft.listener.service.balance

import com.rarible.core.application.ApplicationEnvironmentInfo
import com.rarible.core.entity.reducer.service.EventReduceService
import com.rarible.protocol.solana.nft.listener.consumer.LogRecordEventListenerId
import com.rarible.protocol.solana.nft.listener.consumer.LogRecordEventListener
import com.rarible.protocol.solana.nft.listener.consumer.SolanaLogRecordEvent
import com.rarible.protocol.solana.common.records.SolanaBalanceRecord
import com.rarible.protocol.solana.nft.listener.service.subscribers.SubscriberGroup
import org.springframework.stereotype.Component

@Component
class BalanceEventReduceService(
    entityService: BalanceUpdateService,
    entityIdService: BalanceIdService,
    templateProvider: BalanceTemplateProvider,
    reducer: BalanceReducer,
    environmentInfo: ApplicationEnvironmentInfo,
    val converter: BalanceEventConverter
) : LogRecordEventListener {
    private val delegate = EventReduceService(entityService, entityIdService, templateProvider, reducer)

    override val id: String = LogRecordEventListenerId.balanceHistoryListenerId(environmentInfo.name)

    override val subscriberGroup: SubscriberGroup = SubscriberGroup.BALANCE

    override suspend fun onEntityEvents(events: List<SolanaLogRecordEvent>) {
        val balanceEvents = events
            .filter { it.record is SolanaBalanceRecord }
            .flatMap { converter.convert(it.record as SolanaBalanceRecord, it.reversed) }

        delegate.reduceAll(balanceEvents)
    }
}
