package com.rarible.protocol.solana.nft.listener.service.auction.house

import com.rarible.core.entity.reducer.service.Reducer
import com.rarible.protocol.solana.common.event.AuctionHouseEvent
import com.rarible.protocol.solana.common.event.CreateAuctionHouseEvent
import com.rarible.protocol.solana.common.event.UpdateAuctionHouseEvent
import com.rarible.protocol.solana.common.model.AuctionHouse
import com.rarible.protocol.solana.common.model.isEmpty
import org.springframework.stereotype.Component

@Component
class ForwardValueAuctionHouseReducer : Reducer<AuctionHouseEvent, AuctionHouse> {
    override suspend fun reduce(entity: AuctionHouse, event: AuctionHouseEvent): AuctionHouse {
        if (event !is CreateAuctionHouseEvent && entity.isEmpty) {
            return entity
        }

        val states = if (entity.isEmpty) emptyList() else entity.states + entity.copy(states = emptyList())

        return when (event) {
            is CreateAuctionHouseEvent -> entity.copy(
                createdAt = event.timestamp,
                requiresSignOff = event.requiresSignOff,
                sellerFeeBasisPoints = event.sellerFeeBasisPoints
            )
            is UpdateAuctionHouseEvent -> entity.copy(
                requiresSignOff = event.requiresSignOff ?: entity.requiresSignOff,
                sellerFeeBasisPoints = event.sellerFeeBasisPoints ?: entity.sellerFeeBasisPoints
            )
        }.copy(updatedAt = event.timestamp, states = states)
    }
}