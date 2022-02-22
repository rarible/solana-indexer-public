package com.rarible.protocol.solana.common.converter

import com.rarible.protocol.solana.common.model.TokenWithMeta
import com.rarible.solana.protocol.dto.TokenDto
import com.rarible.solana.protocol.dto.TokensDto

object TokenWithMetaConverter {
    fun convert(tokenWithMeta: TokenWithMeta): TokenDto {
        val (token, tokenMeta) = tokenWithMeta
        return TokenDto(
            address = token.id,
            supply = token.supply,
            createdAt = token.createdAt,
            updatedAt = token.updatedAt,
            closed = token.isDeleted,
            collection = tokenMeta?.collection?.let { TokenMetaConverter.convert(it) },
            creators = tokenMeta?.creators?.map { TokenMetaConverter.convert(it) }
        )
    }

    fun convert(tokensWithMeta: List<TokenWithMeta>): TokensDto = TokensDto(
        total = tokensWithMeta.size.toLong(),
        tokens = tokensWithMeta.map { convert(it) }
    )
}
