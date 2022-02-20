package com.rarible.protocol.solana.common.converter

import com.rarible.protocol.solana.common.model.Balance
import com.rarible.solana.protocol.dto.BalanceDto
import com.rarible.solana.protocol.dto.BalancesDto

object BalanceConverter {
    fun convert(balance: Balance): BalanceDto = BalanceDto(
        account = balance.account,
        value = balance.value,
        mint = balance.mint,
        owner = balance.owner,
        createdAt = balance.createdAt,
        updatedAt = balance.updatedAt
    )

    fun convert(balances: List<Balance>): BalancesDto = BalancesDto(
        total = balances.size.toLong(),
        balances = balances.map { convert(it) }
    )

}
