package com.rarible.protocol.solana.nft.listener.service.auction.house

import com.rarible.core.application.ApplicationEnvironmentInfo
import com.rarible.core.entity.reducer.service.EventReduceService
import com.rarible.protocol.solana.common.records.SolanaAuctionHouseRecord
import com.rarible.protocol.solana.common.records.SubscriberGroup
import com.rarible.protocol.solana.nft.listener.consumer.LogRecordEventListener
import com.rarible.protocol.solana.nft.listener.consumer.LogRecordEventListenerId
import com.rarible.protocol.solana.nft.listener.consumer.SolanaLogRecordEvent
import org.springframework.stereotype.Component

@Component
class AuctionHouseEventReduceService(
    entityService: AuctionHouseUpdateService,
    entityIdService: AuctionHouseIdService,
    templateProvider: AuctionHouseTemplateProvider,
    reducer: AuctionHouseReducer,
    environmentInfo: ApplicationEnvironmentInfo,
    private val converter: AuctionHouseEventConverter
) : LogRecordEventListener {
    private val delegate = EventReduceService(entityService, entityIdService, templateProvider, reducer)

    override val id: String = LogRecordEventListenerId.auctionHousePropertiesListenerId(environmentInfo.name)

    override val subscriberGroup: SubscriberGroup = SubscriberGroup.AUCTION_HOUSE

    override suspend fun onEntityEvents(events: List<SolanaLogRecordEvent>) {
        val auctionHouseEvents = events
            .filter { it.record is SolanaAuctionHouseRecord }
            .flatMap { converter.convert(it.record as SolanaAuctionHouseRecord, it.reversed) }

        delegate.reduceAll(auctionHouseEvents)
    }
}
