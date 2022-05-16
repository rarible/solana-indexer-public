package com.rarible.protocol.solana.nft.listener.service.escrow

import com.rarible.protocol.solana.common.event.EscrowEvent
import com.rarible.protocol.solana.common.model.Escrow
import com.rarible.protocol.solana.common.model.EscrowId
import com.rarible.protocol.solana.nft.listener.service.token.EntityChainReducer
import org.springframework.stereotype.Component

@Component
class ForwardChainEscrowReducer(
    eventApplyPolicy: EscrowConfirmEventApplyPolicy,
    forwardEscrowReducer: ForwardEscrowReducer
) : EntityChainReducer<EscrowId, EscrowEvent, Escrow>(
    eventApplyPolicy,
    forwardEscrowReducer
)