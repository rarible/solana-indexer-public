package com.rarible.protocol.solana.common.converter

import com.rarible.protocol.solana.common.model.Token
import com.rarible.solana.protocol.dto.TokenDeleteEventDto
import com.rarible.solana.protocol.dto.TokenEventDto
import com.rarible.solana.protocol.dto.TokenUpdateEventDto
import java.util.*

object TokenEventConverter {
    fun convert(token: Token): TokenEventDto {
        val eventId = UUID.randomUUID().toString()
        if (token.isDeleted) {
            return TokenDeleteEventDto(
                eventId = eventId,
                address = token.id
            )
        }
        return TokenUpdateEventDto(
            eventId = eventId,
            address = token.id,
            token = TokenConverter.convert(token)
        )
    }
}
