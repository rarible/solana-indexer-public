package com.rarible.protocol.solana.nft.listener.service.auction.house

import com.rarible.core.entity.reducer.service.Reducer
import com.rarible.protocol.solana.common.event.AuctionHouseEvent
import com.rarible.protocol.solana.common.model.AuctionHouse
import org.springframework.stereotype.Component

@Component
class ReversedValueAuctionHouseReducer : Reducer<AuctionHouseEvent, AuctionHouse> {
    override suspend fun reduce(entity: AuctionHouse, event: AuctionHouseEvent): AuctionHouse {
        val newEntity = entity.states.lastOrNull() ?: return AuctionHouse.empty(event.auctionHouse)
        val newStates = entity.states.dropLast(1)

        return newEntity.copy(states = newStates)
    }
}
