package com.rarible.protocol.solana.common.event

import com.rarible.blockchain.scanner.solana.model.SolanaLog
import java.time.Instant

sealed class BalanceEvent : EntityEvent {
    abstract val timestamp: Instant
    abstract val account: String
    abstract override val reversed: Boolean
    abstract override val log: SolanaLog
    abstract override fun invert(): BalanceEvent
}

data class BalanceIncomeEvent(
    override val timestamp: Instant,
    override val account: String,
    override val reversed: Boolean,
    val amount: Long,
    override val log: SolanaLog
) : BalanceEvent() {
    override fun invert() = BalanceOutcomeEvent(timestamp, account, reversed, amount, log)
}

data class BalanceOutcomeEvent(
    override val timestamp: Instant,
    override val account: String,
    override val reversed: Boolean,
    val amount: Long,
    override val log: SolanaLog
) : BalanceEvent() {
    override fun invert() = BalanceIncomeEvent(timestamp, account, reversed, amount, log)
}
