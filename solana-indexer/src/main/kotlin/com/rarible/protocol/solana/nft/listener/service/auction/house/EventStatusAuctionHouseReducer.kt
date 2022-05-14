package com.rarible.protocol.solana.nft.listener.service.auction.house

import com.rarible.core.entity.reducer.service.Reducer
import com.rarible.protocol.solana.common.event.AuctionHouseEvent
import com.rarible.protocol.solana.common.model.AuctionHouse
import org.springframework.stereotype.Component

@Component
class EventStatusAuctionHouseReducer(
    private val forwardChainAuctionHouseReducer: ForwardChainAuctionHouseReducer,
    private val reversedChainAuctionHouseReducer: ReversedChainAuctionHouseReducer,
) : Reducer<AuctionHouseEvent, AuctionHouse> {
    override suspend fun reduce(entity: AuctionHouse, event: AuctionHouseEvent): AuctionHouse {
        return if (event.reversed) {
            reversedChainAuctionHouseReducer.reduce(entity, event)
        } else {
            forwardChainAuctionHouseReducer.reduce(entity, event)
        }
    }
}