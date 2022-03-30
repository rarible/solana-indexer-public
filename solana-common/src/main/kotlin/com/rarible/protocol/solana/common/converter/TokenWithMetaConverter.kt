package com.rarible.protocol.solana.common.converter

import com.rarible.protocol.solana.common.model.TokenWithMeta
import com.rarible.protocol.solana.dto.TokenDto

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
            creators = tokenMeta?.creators?.map { TokenMetaConverter.convert(it) },
            decimals = token.decimals
        )
    }
}
