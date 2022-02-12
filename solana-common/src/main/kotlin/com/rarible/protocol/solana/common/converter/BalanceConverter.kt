package com.rarible.protocol.solana.common.converter

import com.rarible.protocol.solana.common.model.Balance
import com.rarible.solana.protocol.dto.BalanceDto

object BalanceConverter {
    fun convert(balance: Balance): BalanceDto = BalanceDto(
        account = balance.account,
        value = balance.value.toBigInteger(),
        createdAt = balance.createdAt,
        updatedAt = balance.updatedAt
    )
}
