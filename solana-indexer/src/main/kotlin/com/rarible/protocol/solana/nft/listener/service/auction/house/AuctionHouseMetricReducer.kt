package com.rarible.protocol.solana.nft.listener.service.auction.house

import com.rarible.protocol.solana.common.configuration.SolanaIndexerProperties
import com.rarible.protocol.solana.common.event.AuctionHouseEvent
import com.rarible.protocol.solana.common.event.CreateAuctionHouseEvent
import com.rarible.protocol.solana.common.event.UpdateAuctionHouseEvent
import com.rarible.protocol.solana.common.model.AuctionHouse
import com.rarible.protocol.solana.nft.listener.service.AbstractMetricReducer
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Component

@Component
class AuctionHouseMetricReducer(
    properties: SolanaIndexerProperties,
    meterRegistry: MeterRegistry,
) : AbstractMetricReducer<AuctionHouseEvent, AuctionHouse>(meterRegistry, properties, "auction-house") {

    override fun getMetricName(event: AuctionHouseEvent): String {
        return when (event) {
            is CreateAuctionHouseEvent -> "create_auction_house"
            is UpdateAuctionHouseEvent -> "update_auction_house"
        }
    }
}