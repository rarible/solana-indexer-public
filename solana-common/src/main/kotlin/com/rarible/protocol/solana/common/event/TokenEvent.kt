package com.rarible.protocol.solana.common.event

import com.rarible.blockchain.scanner.solana.model.SolanaLog
import com.rarible.protocol.solana.common.model.MetaplexTokenMeta
import java.time.Instant

sealed class TokenEvent : EntityEvent {
    abstract val timestamp: Instant
    abstract val token: String
    abstract override fun invert(): TokenEvent
}

data class InitializeMintEvent(
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
    val amount: Long,
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
    val amount: Long
) : TokenEvent() {
    override fun invert() = MintEvent(
        token = token,
        log = log,
        reversed = reversed,
        timestamp = timestamp,
        amount = amount
    )
}

data class TransferEvent(
    override val log: SolanaLog,
    override val reversed: Boolean,
    override val token: String,
    override val timestamp: Instant,
    val from: String,
    val to: String,
    val amount: Long
) : TokenEvent() {
    override fun invert() = TransferEvent(
        log = log,
        reversed = reversed,
        token = token,
        timestamp = timestamp,
        from = to,
        to = from,
        amount = amount
    )
}

data class MetaplexCreateMetadataEvent(
    override val log: SolanaLog,
    override val reversed: Boolean,
    override val token: String,
    override val timestamp: Instant,
    val metadata: MetaplexTokenMeta
) : TokenEvent() {
    override fun invert() = this
}
