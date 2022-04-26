package com.rarible.protocol.solana.common.converter

import com.rarible.protocol.solana.common.model.Balance
import com.rarible.protocol.solana.common.model.BalanceWithMeta
import com.rarible.protocol.solana.common.pubkey.ProgramDerivedAddressCalc
import com.rarible.protocol.solana.common.pubkey.PublicKey
import com.rarible.protocol.solana.dto.BalanceDto

object BalanceWithMetaConverter {

    fun convert(balanceWithMeta: BalanceWithMeta): BalanceDto {
        val (balance, meta) = balanceWithMeta
        return BalanceDto(
            account = balance.account,
            value = balance.value,
            isAssociatedTokenAccount = isAssociatedTokenAccount(balance),
            mint = balance.mint,
            owner = balance.owner,
            createdAt = balance.createdAt,
            updatedAt = balance.updatedAt,
            collection = meta?.collection?.let { TokenMetaConverter.convert(it) }
        )
    }

    private fun isAssociatedTokenAccount(balance: Balance): Boolean {
        // TODO: the empty checks are not necessary but I have seen some strange balances in the DB (need to recheck).
        val owner = PublicKey(balance.owner.takeIf { it.isNotEmpty() } ?: return false)
        val mint = PublicKey(balance.mint.takeIf { it.isNotEmpty() } ?: return false)
        val account = PublicKey(balance.account)
        return account == ProgramDerivedAddressCalc.getAssociatedTokenAccount(mint, owner).address
    }
}
