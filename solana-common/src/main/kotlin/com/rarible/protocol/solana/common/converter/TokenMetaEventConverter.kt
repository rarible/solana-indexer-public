package com.rarible.protocol.solana.common.converter

import com.rarible.protocol.solana.dto.TokenMetaTriggerEventDto

object TokenMetaEventConverter {

    fun convertTriggerEvent(
        tokenAddress: String
    ): TokenMetaTriggerEventDto = TokenMetaTriggerEventDto(
        tokenAddress = tokenAddress
    )
}