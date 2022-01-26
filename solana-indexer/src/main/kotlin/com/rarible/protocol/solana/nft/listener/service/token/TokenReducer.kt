package com.rarible.protocol.solana.nft.listener.service.token

import com.rarible.core.entity.reducer.chain.combineIntoChain
import com.rarible.core.entity.reducer.service.Reducer
import com.rarible.protocol.solana.nft.listener.model.Token
import org.springframework.stereotype.Component

@Component
class TokenReducer(
    eventStatusTokenReducer: EventStatusTokenReducer,
    tokenMetricReducer: TokenMetricReducer
) : Reducer<TokenEvent, Token> {

    private val eventStatusItemReducer = combineIntoChain(
        LoggingReducer(),
        tokenMetricReducer,
        eventStatusTokenReducer
    )

    override suspend fun reduce(entity: Token, event: TokenEvent): Token {
        return eventStatusItemReducer.reduce(entity, event)
    }
}
