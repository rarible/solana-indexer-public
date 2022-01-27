package com.rarible.protocol.solana.nft.listener.service.balance

import com.rarible.protocol.solana.common.event.BalanceEvent
import com.rarible.protocol.solana.common.model.Balance
import com.rarible.protocol.solana.common.model.BalanceId
import com.rarible.protocol.solana.nft.listener.service.token.EntityChainReducer
import org.springframework.stereotype.Component

@Component
class ForwardChainBalanceReducer(
    eventApplyPolicy: BalanceConfirmEventApplyPolicy,
    forwardBalanceValueReducer: ForwardBalanceValueReducer
) : EntityChainReducer<BalanceId, BalanceEvent, Balance>(
    eventApplyPolicy,
    forwardBalanceValueReducer
)
