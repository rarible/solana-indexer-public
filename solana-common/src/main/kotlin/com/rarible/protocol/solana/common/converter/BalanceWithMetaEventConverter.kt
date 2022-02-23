package com.rarible.protocol.solana.common.converter

import com.rarible.protocol.solana.common.model.BalanceWithMeta
import com.rarible.solana.protocol.dto.BalanceEventDto
import com.rarible.solana.protocol.dto.BalanceUpdateEventDto
import java.util.*

object BalanceWithMetaEventConverter {
    fun convert(balanceWithMeta: BalanceWithMeta): BalanceEventDto {
        val eventId = UUID.randomUUID().toString()
        return BalanceUpdateEventDto(
            eventId = eventId,
            account = balanceWithMeta.balance.account,
            balance = BalanceWithMetaConverter.convert(balanceWithMeta)
        )
    }
}
