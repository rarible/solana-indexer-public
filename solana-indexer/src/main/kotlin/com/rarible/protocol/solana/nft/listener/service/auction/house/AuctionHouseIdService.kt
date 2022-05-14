package com.rarible.protocol.solana.nft.listener.service.auction.house

import com.rarible.core.entity.reducer.service.EntityIdService
import com.rarible.protocol.solana.common.event.AuctionHouseEvent
import com.rarible.protocol.solana.common.model.AuctionHouseId
import org.springframework.stereotype.Component

@Component
class AuctionHouseIdService : EntityIdService<AuctionHouseEvent, AuctionHouseId> {
    override fun getEntityId(event : AuctionHouseEvent): AuctionHouseId {
        return event.auctionHouse
    }
}