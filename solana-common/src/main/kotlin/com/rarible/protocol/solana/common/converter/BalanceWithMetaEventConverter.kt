package com.rarible.protocol.solana.common.converter

import com.rarible.protocol.solana.common.model.BalanceWithMeta
import com.rarible.protocol.solana.dto.BalanceDeleteEventDto
import com.rarible.protocol.solana.dto.BalanceEventDto
import com.rarible.protocol.solana.dto.BalanceUpdateEventDto
import java.math.BigInteger
import java.util.*

object BalanceWithMetaEventConverter {

    fun convert(balanceWithMeta: BalanceWithMeta): BalanceEventDto {
        val eventId = UUID.randomUUID().toString()
        val balance = balanceWithMeta.balance
        return if (balance.value > BigInteger.ZERO) {
            BalanceUpdateEventDto(
                eventId = eventId,
                mint = balance.mint,
                account = balance.account,
                balance = BalanceWithMetaConverter.convert(balanceWithMeta)
            )
        } else {
            BalanceDeleteEventDto(
                eventId = eventId,
                mint = balance.mint,
                account = balance.account,
                balance = BalanceWithMetaConverter.convert(balanceWithMeta)
            )
        }
    }
}
