package com.rarible.protocol.solana.nft.listener.service.token

import com.rarible.core.entity.reducer.service.EntityTemplateProvider
import com.rarible.protocol.solana.common.model.Token
import com.rarible.protocol.solana.common.model.TokenId
import org.springframework.stereotype.Component

@Component
class TokenTemplateProvider : EntityTemplateProvider<TokenId, Token> {
    override fun getEntityTemplate(id: TokenId): Token = Token.empty(id)
}
