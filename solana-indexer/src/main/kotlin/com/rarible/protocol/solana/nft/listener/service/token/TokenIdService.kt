package com.rarible.protocol.solana.nft.listener.service.token

import com.rarible.core.entity.reducer.service.EntityIdService
import com.rarible.protocol.solana.nft.listener.model.TokenId
import org.springframework.stereotype.Component

@Component
class TokenIdService : EntityIdService<TokenEvent, TokenId> {
    override fun getEntityId(event : TokenEvent): TokenId {
        return event.token
    }
}
