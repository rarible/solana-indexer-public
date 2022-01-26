package com.rarible.protocol.solana.nft.listener.service.token

import com.rarible.protocol.solana.nft.listener.consumer.SolanaLogRecordEvent
import com.rarible.protocol.solana.nft.listener.model.Token
import com.rarible.protocol.solana.nft.listener.model.TokenId
import org.springframework.stereotype.Component

@Component
class ForwardChainItemReducer(
    tokenConfirmEventApplyPolicy: TokenConfirmEventApplyPolicy,
    forwardValueTokenReducer: ForwardValueTokenReducer,
) : EntityChainReducer<TokenId, SolanaLogRecordEvent, Token>(
    tokenConfirmEventApplyPolicy,
    forwardValueTokenReducer
)
