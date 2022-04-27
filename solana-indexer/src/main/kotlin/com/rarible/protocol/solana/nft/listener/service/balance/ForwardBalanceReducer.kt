package com.rarible.protocol.solana.nft.listener.service.balance

import com.rarible.core.entity.reducer.service.Reducer
import com.rarible.protocol.solana.common.event.BalanceChangeOwnerEvent
import com.rarible.protocol.solana.common.event.BalanceEvent
import com.rarible.protocol.solana.common.event.BalanceIncomeEvent
import com.rarible.protocol.solana.common.event.BalanceInitializeAccountEvent
import com.rarible.protocol.solana.common.event.BalanceOutcomeEvent
import com.rarible.protocol.solana.common.model.Balance
import com.rarible.protocol.solana.common.model.isEmpty
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class ForwardBalanceReducer : Reducer<BalanceEvent, Balance> {
    private val logger = LoggerFactory.getLogger(javaClass)

    override suspend fun reduce(entity: Balance, event: BalanceEvent): Balance {
        if (event !is BalanceInitializeAccountEvent && entity.isEmpty) {
            return entity
        }
        return when (event) {
            is BalanceInitializeAccountEvent -> entity.copy(
                createdAt = event.timestamp,
                owner = event.owner,
                mint = event.mint
            )
            is BalanceIncomeEvent -> entity.copy(
                value = entity.value + event.amount
            )
            is BalanceOutcomeEvent -> entity.copy(
                value = entity.value - event.amount
            )
            is BalanceChangeOwnerEvent -> {
                if (entity.owner != event.oldOwner) {
                    logger.error("Unexpected owner of $entity, expected: ${event.oldOwner}, actual: ${entity.owner}, for log: ${event.log}")

                    entity
                } else {
                    entity.copy(owner = event.newOwner)
                }
            }
        }.copy(updatedAt = event.timestamp)
    }
}
