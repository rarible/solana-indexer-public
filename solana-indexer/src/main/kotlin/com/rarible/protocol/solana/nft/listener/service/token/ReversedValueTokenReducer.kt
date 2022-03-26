package com.rarible.protocol.solana.nft.listener.service.token

import com.rarible.core.entity.reducer.service.Reducer
import com.rarible.protocol.solana.common.event.TokenEvent
import com.rarible.protocol.solana.common.model.Token
import org.springframework.stereotype.Component

@Component
class ReversedValueTokenReducer : Reducer<TokenEvent, Token> {
    private val forwardValueTokenReducer = ForwardValueTokenReducer()

    override suspend fun reduce(entity: Token, event: TokenEvent): Token {
        return forwardValueTokenReducer.reduce(entity, event.invert())
    }
}

