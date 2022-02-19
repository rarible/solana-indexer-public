package com.rarible.protocol.solana.nft.listener.service.balance

import com.rarible.core.entity.reducer.service.Reducer
import com.rarible.protocol.solana.common.event.BalanceEvent
import com.rarible.protocol.solana.common.model.Balance
import org.springframework.stereotype.Component

@Component
class ReversedBalanceValueReducer : Reducer<BalanceEvent, Balance> {
    private val forwardValueTokenReducer = ForwardBalanceReducer()

    override suspend fun reduce(entity: Balance, event: BalanceEvent): Balance {
        val invert = event.invert() ?: return Balance.empty(account = entity.account)
        return forwardValueTokenReducer.reduce(entity, invert)
    }
}
