package com.rarible.protocol.solana.common.event

import com.rarible.blockchain.scanner.solana.model.SolanaLog
import java.math.BigInteger
import java.time.Instant

sealed class TokenEvent : EntityEvent {
    abstract val timestamp: Instant
    abstract val token: String
    abstract fun invert(): TokenEvent
}

data class CreateMetaEvent(
    override val log: SolanaLog,
    override val reversed: Boolean,
    override val timestamp: Instant,
    override val token: String
) : TokenEvent() {
    override fun invert(): TokenEvent = throw UnsupportedOperationException("Can't invert CreateMetaEvent")
}

data class InitializeMintEvent(
    val decimals: Int,
    override val log: SolanaLog,
    override val reversed: Boolean,
    override val token: String,
    override val timestamp: Instant
) : TokenEvent() {
    override fun invert(): TokenEvent = this.copy(
        timestamp = Instant.EPOCH
    )
}

data class MintEvent(
    override val log: SolanaLog,
    override val reversed: Boolean,
    override val token: String,
    override val timestamp: Instant,
    val amount: BigInteger,
) : TokenEvent() {
    override fun invert() = BurnEvent(
        token = token,
        log = log,
        reversed = reversed,
        timestamp = timestamp,
        amount = amount
    )
}

data class BurnEvent(
    override val log: SolanaLog,
    override val reversed: Boolean,
    override val token: String,
    override val timestamp: Instant,
    val amount: BigInteger
) : TokenEvent() {
    override fun invert() = MintEvent(
        token = token,
        log = log,
        reversed = reversed,
        timestamp = timestamp,
        amount = amount
    )
}
