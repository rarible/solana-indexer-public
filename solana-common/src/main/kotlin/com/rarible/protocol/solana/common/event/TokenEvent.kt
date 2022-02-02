package com.rarible.protocol.solana.common.event

import com.rarible.blockchain.scanner.solana.model.SolanaLog

sealed class TokenEvent : EntityEvent {
    abstract val token: String
    abstract override fun invert(): TokenEvent
}

data class MintEvent(
    override val log: SolanaLog,
    override val reversed: Boolean,
    override val token: String,
    val amount: Long
) : TokenEvent() {
    override fun invert() = BurnEvent(
        token = token,
        log = log,
        reversed = reversed,
        amount = amount
    )
}

data class BurnEvent(
    override val log: SolanaLog,
    override val reversed: Boolean,
    override val token: String,
    val amount: Long
) : TokenEvent() {
    override fun invert() = MintEvent(
        token = token,
        log = log,
        reversed = reversed,
        amount = amount
    )
}

data class TransferEvent(
    override val log: SolanaLog,
    override val reversed: Boolean,
    override val token: String,
    val from: String,
    val to: String,
    val amount: Long
) : TokenEvent() {
    override fun invert() = TransferEvent(
        token = token,
        log = log,
        reversed = reversed,
        amount = amount,
        from = to,
        to = from
    )
}
