package com.rarible.protocol.solana.nft.listener.service.token

import com.rarible.core.entity.reducer.service.EntityTemplateProvider
import com.rarible.protocol.solana.nft.listener.model.Token
import com.rarible.protocol.solana.nft.listener.model.TokenId
import org.springframework.stereotype.Component

@Component
class TokenTemplateProvider : EntityTemplateProvider<TokenId, Token> {
    override fun getEntityTemplate(id: TokenId): Token {
        return Token.empty(id)
    }
}
