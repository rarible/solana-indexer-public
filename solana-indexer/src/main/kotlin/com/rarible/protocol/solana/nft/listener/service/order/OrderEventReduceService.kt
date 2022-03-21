package com.rarible.protocol.solana.nft.listener.service.order

import com.rarible.core.application.ApplicationEnvironmentInfo
import com.rarible.core.entity.reducer.service.EventReduceService
import com.rarible.protocol.solana.nft.listener.consumer.LogRecordEventListener
import com.rarible.protocol.solana.nft.listener.consumer.LogRecordEventListenerId
import com.rarible.protocol.solana.nft.listener.consumer.SolanaLogRecordEvent
import com.rarible.protocol.solana.nft.listener.service.records.SolanaAuctionHouseRecord
import com.rarible.protocol.solana.nft.listener.service.subscribers.SubscriberGroup
import org.springframework.stereotype.Component

@Component
class OrderEventReduceService(
    entityService: OrderUpdateService,
    entityIdService: OrderIdService,
    templateProvider: OrderTemplateProvider,
    reducer: OrderReducer,
    environmentInfo: ApplicationEnvironmentInfo,
    val converter: OrderEventConverter
) : LogRecordEventListener {
    private val delegate = EventReduceService(entityService, entityIdService, templateProvider, reducer)

    override val id: String = LogRecordEventListenerId.auctionHouseListenerId(environmentInfo.name)

    override val subscriberGroup: SubscriberGroup = SubscriberGroup.AUCTION_HOUSE

    override suspend fun onEntityEvents(events: List<SolanaLogRecordEvent>) {
        val auctionHouseEvents = events
            .filter { it.record is SolanaAuctionHouseRecord }
            .flatMap { converter.convert(it.record as SolanaAuctionHouseRecord, it.reversed) }

        delegate.reduceAll(auctionHouseEvents)
    }
}