package com.rarible.protocol.solana.nft.listener.service.escrow

import com.rarible.protocol.solana.common.configuration.SolanaIndexerProperties
import com.rarible.protocol.solana.common.event.EscrowEvent
import com.rarible.protocol.solana.nft.listener.service.token.ConfirmEventApplyPolicy
import com.rarible.protocol.solana.nft.listener.service.token.RevertEventApplyPolicy
import org.springframework.stereotype.Component

@Component
class EscrowConfirmEventApplyPolicy(properties: SolanaIndexerProperties) :
    ConfirmEventApplyPolicy<EscrowEvent>(properties.confirmationBlocks)

@Component
class EscrowRevertEventApplyPolicy :
    RevertEventApplyPolicy<EscrowEvent>()