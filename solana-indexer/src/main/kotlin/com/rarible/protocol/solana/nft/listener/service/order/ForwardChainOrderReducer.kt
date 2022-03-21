package com.rarible.protocol.solana.nft.listener.service.order

import com.rarible.protocol.solana.common.event.OrderEvent
import com.rarible.protocol.solana.common.model.Order
import com.rarible.protocol.solana.common.model.OrderId
import com.rarible.protocol.solana.nft.listener.service.token.EntityChainReducer
import org.springframework.stereotype.Component

@Component
class ForwardChainOrderReducer(
    eventApplyPolicy: OrderConfirmEventApplyPolicy,
    forwardOrderReducer: ForwardOrderReducer
) : EntityChainReducer<OrderId, OrderEvent, Order>(
    eventApplyPolicy,
    forwardOrderReducer
)