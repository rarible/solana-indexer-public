package com.rarible.protocol.solana.common.converter

import com.rarible.protocol.solana.common.model.Balance
import com.rarible.solana.protocol.dto.BalanceEventDto
import com.rarible.solana.protocol.dto.BalanceUpdateEventDto
import java.util.*

object BalanceEventConverter {
    fun convert(balance: Balance): BalanceEventDto {
        val eventId = UUID.randomUUID().toString()
        return BalanceUpdateEventDto(
            eventId = eventId,
            account = balance.account,
            balance = BalanceConverter.convert(balance)
        )
    }
}
