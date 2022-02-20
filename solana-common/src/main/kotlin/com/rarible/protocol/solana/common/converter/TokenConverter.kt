package com.rarible.protocol.solana.common.converter

import com.rarible.protocol.solana.common.model.Token
import com.rarible.solana.protocol.dto.TokenDto
import com.rarible.solana.protocol.dto.TokensDto

object TokenConverter {
    fun convert(token: Token): TokenDto = TokenDto(
        address = token.id,
        supply = token.supply,
        createdAt = token.createdAt,
        updatedAt = token.updatedAt,
        closed = token.isDeleted
    )

    fun convert(tokens: List<Token>): TokensDto = TokensDto(
        total = tokens.size.toLong(),
        tokens = tokens.map { convert(it) }
    )
}
