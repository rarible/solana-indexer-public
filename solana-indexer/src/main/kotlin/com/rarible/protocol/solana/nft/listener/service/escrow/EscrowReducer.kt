package com.rarible.protocol.solana.nft.listener.service.escrow

import com.rarible.core.entity.reducer.chain.combineIntoChain
import com.rarible.core.entity.reducer.service.Reducer
import com.rarible.protocol.solana.common.event.EscrowEvent
import com.rarible.protocol.solana.common.model.Escrow
import com.rarible.protocol.solana.nft.listener.service.LoggingReducer
import org.springframework.stereotype.Component

@Component
class EscrowReducer(
    eventStatusEscrowReducer: EventStatusEscrowReducer,
    escrowMetricReducer: EscrowMetricReducer
) : Reducer<EscrowEvent, Escrow> {

    private val eventStatusEscrowReducer = combineIntoChain(
        LoggingReducer(),
        escrowMetricReducer,
        eventStatusEscrowReducer
    )

    override suspend fun reduce(entity: Escrow, event: EscrowEvent): Escrow {
        return eventStatusEscrowReducer.reduce(entity, event)
    }
}