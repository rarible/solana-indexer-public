package com.rarible.protocol.solana.nft.listener.service.token

import com.rarible.protocol.solana.common.configuration.SolanaIndexerProperties
import com.rarible.protocol.solana.common.event.TokenEvent
import org.springframework.stereotype.Component

@Component
class TokenConfirmEventApplyPolicy(properties: SolanaIndexerProperties) :
    ConfirmEventApplyPolicy<TokenEvent>(properties.confirmationBlocks)

@Component
class TokenRevertEventApplyPolicy :
    RevertEventApplyPolicy<TokenEvent>()
