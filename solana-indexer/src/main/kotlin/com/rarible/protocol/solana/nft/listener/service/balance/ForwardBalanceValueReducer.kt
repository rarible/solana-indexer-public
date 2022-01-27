package com.rarible.protocol.solana.nft.listener.service.balance

import com.rarible.core.entity.reducer.service.Reducer
import com.rarible.protocol.solana.nft.listener.model.Balance
import org.springframework.stereotype.Component

@Component
class ForwardBalanceValueReducer : Reducer<BalanceEvent, Balance> {
    override suspend fun reduce(entity: Balance, event: BalanceEvent): Balance {
        val value = when (event) {
            is BalanceIncomeEvent -> entity.value + event.amount
            is BalanceOutcomeEvent -> entity.value - event.amount
        }

        return entity.copy(value = value)
    }
}
