package com.rarible.protocol.solana.nft.listener.service.auction.house

import com.rarible.protocol.solana.common.event.AuctionHouseEvent
import com.rarible.protocol.solana.common.model.AuctionHouse
import com.rarible.protocol.solana.common.model.AuctionHouseId
import com.rarible.protocol.solana.nft.listener.service.token.RevertedEntityChainReducer
import org.springframework.stereotype.Component

@Component
class ReversedChainAuctionHouseReducer(
    auctionHouseRevertEventApplyPolicy: AuctionHouseRevertEventApplyPolicy,
    reversedValueAuctionHouseReducer: ReversedValueAuctionHouseReducer,
) : RevertedEntityChainReducer<AuctionHouseId, AuctionHouseEvent, AuctionHouse>(
    auctionHouseRevertEventApplyPolicy,
    reversedValueAuctionHouseReducer
)