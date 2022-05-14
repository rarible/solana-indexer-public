package com.rarible.protocol.solana.nft.listener.service.auction.house

import com.rarible.protocol.solana.common.configuration.SolanaIndexerProperties
import com.rarible.protocol.solana.common.event.AuctionHouseEvent
import com.rarible.protocol.solana.nft.listener.service.token.ConfirmEventApplyPolicy
import com.rarible.protocol.solana.nft.listener.service.token.RevertEventApplyPolicy
import org.springframework.stereotype.Component

@Component
class AuctionHouseConfirmEventApplyPolicy(properties: SolanaIndexerProperties) :
    ConfirmEventApplyPolicy<AuctionHouseEvent>(properties.confirmationBlocks)

@Component
class AuctionHouseRevertEventApplyPolicy :
    RevertEventApplyPolicy<AuctionHouseEvent>()