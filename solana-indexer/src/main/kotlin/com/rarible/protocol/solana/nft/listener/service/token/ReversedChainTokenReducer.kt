package com.rarible.protocol.solana.nft.listener.service.token

import com.rarible.protocol.solana.common.event.TokenEvent
import com.rarible.protocol.solana.common.model.Token
import com.rarible.protocol.solana.common.model.TokenId
import org.springframework.stereotype.Component

@Component
class ReversedChainTokenReducer(
    tokenRevertEventApplyPolicy: TokenRevertEventApplyPolicy,
    reversedValueTokenReducer: ReversedValueTokenReducer,
) : RevertedEntityChainReducer<TokenId, TokenEvent, Token>(
    tokenRevertEventApplyPolicy,
    reversedValueTokenReducer
)