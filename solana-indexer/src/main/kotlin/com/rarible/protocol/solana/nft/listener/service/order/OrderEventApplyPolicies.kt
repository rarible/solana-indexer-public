package com.rarible.protocol.solana.nft.listener.service.order

import com.rarible.protocol.solana.common.configuration.SolanaIndexerProperties
import com.rarible.protocol.solana.common.event.OrderEvent
import com.rarible.protocol.solana.nft.listener.service.token.ConfirmEventApplyPolicy
import com.rarible.protocol.solana.nft.listener.service.token.RevertEventApplyPolicy
import org.springframework.stereotype.Component

@Component
class OrderConfirmEventApplyPolicy(properties: SolanaIndexerProperties) :
    ConfirmEventApplyPolicy<OrderEvent>(properties.confirmationBlocks)

@Component
class OrderRevertEventApplyPolicy :
    RevertEventApplyPolicy<OrderEvent>()