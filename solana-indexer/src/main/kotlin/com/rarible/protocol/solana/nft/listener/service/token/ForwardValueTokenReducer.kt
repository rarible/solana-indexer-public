package com.rarible.protocol.solana.nft.listener.service.token

import com.rarible.core.entity.reducer.service.Reducer
import com.rarible.protocol.solana.nft.listener.model.Token
import org.springframework.stereotype.Component

@Component
class ForwardValueTokenReducer : Reducer<TokenEvent, Token> {
    override suspend fun reduce(entity: Token, event: TokenEvent): Token {
        return when (event) {
            is MintEvent -> entity.copy(supply = entity.supply + event.amount)
            is BurnEvent -> entity.copy(supply = entity.supply - event.amount)
            is TransferEvent -> entity
        }
    }
}