package com.rarible.protocol.solana.nft.listener.service.auction.house

import com.rarible.core.entity.reducer.service.EntityTemplateProvider
import com.rarible.protocol.solana.common.model.AuctionHouse
import com.rarible.protocol.solana.common.model.AuctionHouseId
import org.springframework.stereotype.Component

@Component
class AuctionHouseTemplateProvider: EntityTemplateProvider<AuctionHouseId, AuctionHouse> {
    override fun getEntityTemplate(id: AuctionHouseId): AuctionHouse = AuctionHouse.empty(id)
}