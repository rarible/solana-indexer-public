package com.rarible.protocol.solana.nft.listener.service.token

import com.rarible.protocol.solana.nft.listener.model.Token
import com.rarible.protocol.solana.nft.listener.model.TokenId
import org.springframework.stereotype.Component

@Component
class ReversedChainTokenReducer(
    tokenRevertEventApplyPolicy: TokenRevertEventApplyPolicy,
    reversedValueTokenReducer: ReversedValueTokenReducer,
) : RevertedEntityChainReducer<TokenId, TokenEvent, Token>(
    tokenRevertEventApplyPolicy,
    reversedValueTokenReducer
)