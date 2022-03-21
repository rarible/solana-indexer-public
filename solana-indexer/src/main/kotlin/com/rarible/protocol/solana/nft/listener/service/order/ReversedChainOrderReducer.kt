package com.rarible.protocol.solana.nft.listener.service.order

import com.rarible.protocol.solana.common.event.OrderEvent
import com.rarible.protocol.solana.common.model.Order
import com.rarible.protocol.solana.common.model.OrderId
import com.rarible.protocol.solana.nft.listener.service.token.RevertedEntityChainReducer
import org.springframework.stereotype.Component

@Component
class ReversedChainOrderReducer(
    eventApplyPolicy: OrderRevertEventApplyPolicy,
    reversedOrderValueReducer: ReversedOrderValueReducer,
) : RevertedEntityChainReducer<OrderId, OrderEvent, Order>(
    eventApplyPolicy,
    reversedOrderValueReducer
)
