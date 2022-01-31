package com.rarible.protocol.solana.nft.listener.service.token

import com.rarible.core.entity.reducer.service.Reducer
import com.rarible.protocol.solana.common.event.TokenEvent
import com.rarible.protocol.solana.common.model.Token
import org.springframework.stereotype.Component

@Component
class EventStatusTokenReducer(
    private val forwardChainTokenReducer: ForwardChainTokenReducer,
    private val reversedChainTokenReducer: ReversedChainTokenReducer,
) : Reducer<TokenEvent, Token> {
    override suspend fun reduce(entity: Token, event: TokenEvent): Token {
        return if (event.reversed) {
            reversedChainTokenReducer.reduce(entity, event)
        } else {
            forwardChainTokenReducer.reduce(entity, event)
        }
    }
}