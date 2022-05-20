package com.rarible.protocol.solana.nft.listener.service.escrow

import com.rarible.core.entity.reducer.service.Reducer
import com.rarible.protocol.solana.common.event.EscrowBuyEvent
import com.rarible.protocol.solana.common.event.EscrowDepositEvent
import com.rarible.protocol.solana.common.event.EscrowEvent
import com.rarible.protocol.solana.common.event.EscrowExecuteSaleEvent
import com.rarible.protocol.solana.common.event.EscrowWithdrawEvent
import com.rarible.protocol.solana.common.model.Escrow
import com.rarible.protocol.solana.common.model.isEmpty
import org.springframework.stereotype.Component

@Component
class ForwardEscrowReducer : Reducer<EscrowEvent, Escrow> {
    override suspend fun reduce(entity: Escrow, event: EscrowEvent): Escrow {
        val states = if (entity.isEmpty) emptyList() else entity.states + entity.copy(states = emptyList())

        return when (event) {
            is EscrowBuyEvent -> if (entity.isEmpty) {
                entity.copy(
                    wallet = event.wallet,
                    auctionHouse = event.auctionHouse,
                    account = event.account,
                    createdAt = event.timestamp,
                    value = entity.value.max(event.amount)
                )
            } else {
                entity.copy(
                    value = entity.value.max(event.amount)
                )
            }
            is EscrowDepositEvent -> if (entity.isEmpty) {
                entity.copy(
                    wallet = event.wallet,
                    auctionHouse = event.auctionHouse,
                    account = event.account,
                    createdAt = event.timestamp,
                    value = entity.value + event.amount
                )
            } else {
                entity.copy(
                    value = entity.value + event.amount
                )
            }
            is EscrowExecuteSaleEvent -> entity.copy(
                value = entity.value - event.amount
            )
            is EscrowWithdrawEvent -> entity.copy(
                value = entity.value - event.amount
            )
        }.copy(updatedAt = event.timestamp, states = states, lastEvent = event)
    }
}
