package com.rarible.protocol.solana.nft.listener.service.balance

import com.rarible.blockchain.scanner.solana.model.SolanaLog
import com.rarible.protocol.solana.nft.listener.model.EntityEvent

sealed class BalanceEvent : EntityEvent {
    abstract val account: String
    abstract override val reversed: Boolean
    abstract override val log: SolanaLog
    abstract override fun invert(): BalanceEvent
}

class BalanceIncomeEvent(
    override val account: String,
    override val reversed: Boolean,
    val amount: Long,
    override val log: SolanaLog
) : BalanceEvent() {
    override fun invert() = BalanceOutcomeEvent(account, reversed, amount, log)
}

class BalanceOutcomeEvent(
    override val account: String,
    override val reversed: Boolean,
    val amount: Long,
    override val log: SolanaLog
) : BalanceEvent() {
    override fun invert() = BalanceIncomeEvent(account, reversed, amount, log)
}