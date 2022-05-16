package com.rarible.protocol.solana.nft.listener.service.escrow

import com.rarible.core.entity.reducer.service.Reducer
import com.rarible.protocol.solana.common.event.EscrowEvent
import com.rarible.protocol.solana.common.model.Escrow
import org.springframework.stereotype.Component

@Component
class ReversedEscrowValueReducer : Reducer<EscrowEvent, Escrow> {
    override suspend fun reduce(entity: Escrow, event: EscrowEvent): Escrow {
        val newEntity = entity.states.lastOrNull() ?: return Escrow.empty()
        val newStates = entity.states.dropLast(1)

        return newEntity.copy(states = newStates)
    }
}