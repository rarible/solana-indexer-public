package com.rarible.protocol.solana.nft.listener.service.balance

import com.rarible.core.entity.reducer.service.Reducer
import com.rarible.protocol.solana.common.event.BalanceEvent
import com.rarible.protocol.solana.common.event.BalanceIncomeEvent
import com.rarible.protocol.solana.common.event.BalanceOutcomeEvent
import com.rarible.protocol.solana.common.model.Balance
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class ForwardBalanceValueReducer : Reducer<BalanceEvent, Balance> {
    override suspend fun reduce(entity: Balance, event: BalanceEvent): Balance {
        val value = when (event) {
            is BalanceIncomeEvent -> entity.value + event.amount
            is BalanceOutcomeEvent -> entity.value - event.amount
        }

        val createdAt = if (entity.createdAt == Instant.EPOCH) {
            event.timestamp
        } else {
            entity.createdAt
        }

        return entity.copy(
            value = value,
            createdAt = createdAt,
            updatedAt = event.timestamp
        )
    }
}
