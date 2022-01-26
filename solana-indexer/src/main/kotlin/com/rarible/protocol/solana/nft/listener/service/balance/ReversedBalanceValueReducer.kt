package com.rarible.protocol.solana.nft.listener.service.balance

import com.rarible.core.entity.reducer.service.Reducer
import com.rarible.protocol.solana.nft.listener.model.Balance
import org.springframework.stereotype.Component

@Component
class ReversedBalanceValueReducer : Reducer<BalanceEvent, Balance> {
    private val forwardValueTokenReducer = ForwardBalanceValueReducer()

    override suspend fun reduce(entity: Balance, event: BalanceEvent): Balance {
        return forwardValueTokenReducer.reduce(entity, event.invert())
    }
}