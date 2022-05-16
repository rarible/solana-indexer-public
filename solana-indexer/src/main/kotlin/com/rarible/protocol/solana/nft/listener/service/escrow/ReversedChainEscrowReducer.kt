package com.rarible.protocol.solana.nft.listener.service.escrow

import com.rarible.protocol.solana.common.event.EscrowEvent
import com.rarible.protocol.solana.common.model.Escrow
import com.rarible.protocol.solana.common.model.EscrowId
import com.rarible.protocol.solana.nft.listener.service.token.RevertedEntityChainReducer
import org.springframework.stereotype.Component

@Component
class ReversedChainEscrowReducer(
    eventApplyPolicy: EscrowRevertEventApplyPolicy,
    reversedEscrowValueReducer: ReversedEscrowValueReducer,
) : RevertedEntityChainReducer<EscrowId, EscrowEvent, Escrow>(
    eventApplyPolicy,
    reversedEscrowValueReducer
)
