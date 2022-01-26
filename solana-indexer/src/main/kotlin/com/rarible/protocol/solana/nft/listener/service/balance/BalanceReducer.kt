package com.rarible.protocol.solana.nft.listener.service.balance

import com.rarible.core.entity.reducer.chain.combineIntoChain
import com.rarible.core.entity.reducer.service.Reducer
import com.rarible.protocol.solana.nft.listener.model.Balance
import org.springframework.stereotype.Component

@Component
class BalanceReducer(
    eventStatusBalanceReducer: EventStatusBalanceReducer,
    balanceMetricReducer: BalanceMetricReducer
) : Reducer<BalanceEvent, Balance> {

    private val eventStatusBalanceReducer = combineIntoChain<BalanceEvent, Balance>(
//        LoggingReducer(),
        balanceMetricReducer,
        eventStatusBalanceReducer
    )

    override suspend fun reduce(entity: Balance, event: BalanceEvent): Balance {
        return eventStatusBalanceReducer.reduce(entity, event)
    }
}
