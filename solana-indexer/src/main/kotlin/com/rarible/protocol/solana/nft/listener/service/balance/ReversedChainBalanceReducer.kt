package com.rarible.protocol.solana.nft.listener.service.balance

import com.rarible.protocol.solana.common.event.BalanceEvent
import com.rarible.protocol.solana.common.model.Balance
import com.rarible.protocol.solana.common.model.BalanceId
import com.rarible.protocol.solana.nft.listener.service.token.RevertedEntityChainReducer
import org.springframework.stereotype.Component

@Component
class ReversedChainBalanceReducer(
    eventApplyPolicy: BalanceRevertEventApplyPolicy,
    reversedBalanceValueReducer: ReversedBalanceValueReducer,
) : RevertedEntityChainReducer<BalanceId, BalanceEvent, Balance>(
    eventApplyPolicy,
    reversedBalanceValueReducer
)
