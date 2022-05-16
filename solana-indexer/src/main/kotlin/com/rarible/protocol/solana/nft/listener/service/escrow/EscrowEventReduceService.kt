package com.rarible.protocol.solana.nft.listener.service.escrow

import com.rarible.core.application.ApplicationEnvironmentInfo
import com.rarible.core.entity.reducer.service.EventReduceService
import com.rarible.protocol.solana.common.records.SolanaEscrowRecord
import com.rarible.protocol.solana.common.records.SubscriberGroup
import com.rarible.protocol.solana.nft.listener.consumer.LogRecordEventListener
import com.rarible.protocol.solana.nft.listener.consumer.LogRecordEventListenerId
import com.rarible.protocol.solana.nft.listener.consumer.SolanaLogRecordEvent
import org.springframework.stereotype.Component

@Component
class EscrowEventReduceService(
    entityService: EscrowUpdateService,
    entityIdService: EscrowIdService,
    templateProvider: EscrowTemplateProvider,
    reducer: EscrowReducer,
    environmentInfo: ApplicationEnvironmentInfo,
    private val converter: EscrowEventConverter
) : LogRecordEventListener {

    private val delegate = EventReduceService(entityService, entityIdService, templateProvider, reducer)

    override val id: String = LogRecordEventListenerId.escrowListenerId(environmentInfo.name)

    override val subscriberGroup: SubscriberGroup = SubscriberGroup.ESCROW

    override suspend fun onEntityEvents(events: List<SolanaLogRecordEvent>) {
        val escrowEvents = events
            .filter { it.record is SolanaEscrowRecord }
            .flatMap { converter.convert(it.record as SolanaEscrowRecord, it.reversed) }

        delegate.reduceAll(escrowEvents)
    }
}
