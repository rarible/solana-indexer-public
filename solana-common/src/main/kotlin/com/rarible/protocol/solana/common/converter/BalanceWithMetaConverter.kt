package com.rarible.protocol.solana.common.converter

import com.rarible.protocol.solana.common.model.BalanceWithMeta
import com.rarible.protocol.solana.dto.BalanceDto
import com.rarible.protocol.solana.dto.BalancesDto

object BalanceWithMetaConverter {
    fun convert(balanceWithMeta: BalanceWithMeta): BalanceDto {
        val (balance, meta) = balanceWithMeta
        return BalanceDto(
            account = balance.account,
            value = balance.value,
            mint = balance.mint,
            owner = balance.owner,
            createdAt = balance.createdAt,
            updatedAt = balance.updatedAt,
            collection = meta?.collection?.let { TokenMetaConverter.convert(it) }
        )
    }

    fun convert(balances: List<BalanceWithMeta>): BalancesDto = BalancesDto(
        balances = balances.map { convert(it) }
        // TODO add continuation
    )

}
