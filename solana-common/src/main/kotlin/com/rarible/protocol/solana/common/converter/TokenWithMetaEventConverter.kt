package com.rarible.protocol.solana.common.converter

import com.rarible.protocol.solana.common.model.TokenWithMeta
import com.rarible.solana.protocol.dto.TokenDeleteEventDto
import com.rarible.solana.protocol.dto.TokenEventDto
import com.rarible.solana.protocol.dto.TokenUpdateEventDto
import java.util.*

object TokenWithMetaEventConverter {
    fun convert(tokenWithMeta: TokenWithMeta): TokenEventDto {
        val eventId = UUID.randomUUID().toString()
        if (tokenWithMeta.token.isDeleted) {
            return TokenDeleteEventDto(
                eventId = eventId,
                address = tokenWithMeta.token.id
            )
        }
        return TokenUpdateEventDto(
            eventId = eventId,
            address = tokenWithMeta.token.id,
            token = TokenWithMetaConverter.convert(tokenWithMeta)
        )
    }
}
