package com.rarible.protocol.solana.common.converter

import com.rarible.protocol.solana.common.model.TokenWithMeta
import com.rarible.protocol.solana.dto.TokenEventDto
import com.rarible.protocol.solana.dto.TokenUpdateEventDto
import java.util.*

object TokenWithMetaEventConverter {
    fun convert(tokenWithMeta: TokenWithMeta): TokenEventDto {
        val eventId = UUID.randomUUID().toString()
        return TokenUpdateEventDto(
            eventId = eventId,
            address = tokenWithMeta.token.id,
            token = TokenWithMetaConverter.convert(tokenWithMeta)
        )
    }
}
