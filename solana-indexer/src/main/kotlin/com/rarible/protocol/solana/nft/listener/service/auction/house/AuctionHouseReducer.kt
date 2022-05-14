package com.rarible.protocol.solana.nft.listener.service.auction.house

import com.rarible.core.entity.reducer.chain.combineIntoChain
import com.rarible.core.entity.reducer.service.Reducer
import com.rarible.protocol.solana.common.event.AuctionHouseEvent
import com.rarible.protocol.solana.common.model.AuctionHouse
import com.rarible.protocol.solana.nft.listener.service.LoggingReducer
import org.springframework.stereotype.Component

@Component
class AuctionHouseReducer(
    eventStatusAuctionHouseReducer: EventStatusAuctionHouseReducer,
    auctionHouseMetricReducer: AuctionHouseMetricReducer
) : Reducer<AuctionHouseEvent, AuctionHouse> {

    private val eventStatusAuctionHouseReducer = combineIntoChain(
        LoggingReducer(),
        auctionHouseMetricReducer,
        eventStatusAuctionHouseReducer
    )

    override suspend fun reduce(entity: AuctionHouse, event: AuctionHouseEvent): AuctionHouse {
        return eventStatusAuctionHouseReducer.reduce(entity, event)
    }
}