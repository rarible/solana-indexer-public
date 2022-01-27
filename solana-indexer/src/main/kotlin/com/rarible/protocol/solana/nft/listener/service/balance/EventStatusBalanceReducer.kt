package com.rarible.protocol.solana.nft.listener.service.balance

import com.rarible.core.entity.reducer.service.Reducer
import com.rarible.protocol.solana.common.event.BalanceEvent
import com.rarible.protocol.solana.common.model.Balance
import org.springframework.stereotype.Component

@Component
class EventStatusBalanceReducer(
    private val forwardBalanceReducer: ForwardChainBalanceReducer,
    private val reversedBalanceReducer: ReversedChainBalanceReducer,
) : Reducer<BalanceEvent, Balance> {

    override suspend fun reduce(entity: Balance, event: BalanceEvent): Balance {
        return if (event.reversed) {
            reversedBalanceReducer.reduce(entity, event)
        } else {
            forwardBalanceReducer.reduce(entity, event)
        }
    }
}
