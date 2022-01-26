package com.rarible.protocol.solana.nft.listener.service.token

import com.rarible.core.entity.reducer.service.Reducer
import com.rarible.protocol.solana.nft.listener.consumer.SolanaLogRecordEvent
import com.rarible.protocol.solana.nft.listener.model.Token
import org.springframework.stereotype.Component

@Component
class EventStatusTokenReducer(
    private val forwardChainItemReducer: ForwardChainItemReducer,
    private val reversedChainTokenReducer: ReversedChainTokenReducer,
) : Reducer<SolanaLogRecordEvent, Token> {
    override suspend fun reduce(entity: Token, event: SolanaLogRecordEvent): Token {
        return if (event.reversed) {
            reversedChainTokenReducer.reduce(entity, event)
        } else {
            forwardChainItemReducer.reduce(entity, event)
        }
    }
}