package com.rarible.protocol.solana.common.converter

import com.rarible.protocol.solana.common.model.Balance
import com.rarible.protocol.solana.common.model.BalanceWithMeta
import com.rarible.solana.protocol.dto.BalanceDto
import com.rarible.solana.protocol.dto.BalancesDto

object BalanceConverter {
    fun convert(balanceWithMeta: BalanceWithMeta): BalanceDto {
        val (balance, meta) = balanceWithMeta

        return BalanceDto(
            account = balance.account,
            value = balance.value,
            mint = balance.mint,
            owner = balance.owner,
            createdAt = balance.createdAt,
            updatedAt = balance.updatedAt,
            collection = meta?.collection?.let { TokenMetaConverter.convert(it) },
            creators = meta?.creators?.map { TokenMetaConverter.convert(it) }
        )
    }

    fun convert(balances: List<BalanceWithMeta>): BalancesDto = BalancesDto(
        total = balances.size.toLong(),
        balances = balances.map { convert(it) }
    )

}
