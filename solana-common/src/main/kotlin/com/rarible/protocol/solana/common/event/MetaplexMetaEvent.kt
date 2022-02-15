package com.rarible.protocol.solana.common.event

import com.rarible.blockchain.scanner.solana.model.SolanaLog
import com.rarible.protocol.solana.common.model.MetaplexTokenMeta
import java.time.Instant

sealed class MetaplexMetaEvent : EntityEvent {
    abstract val timestamp: Instant
    abstract val metaAddress: String
    abstract override fun invert(): MetaplexMetaEvent
}

data class MetaplexCreateMetadataEvent(
    override val log: SolanaLog,
    override val reversed: Boolean,
    override val metaAddress: String,
    override val timestamp: Instant,
    val token: String,
    val metadata: MetaplexTokenMeta
) : MetaplexMetaEvent() {
    override fun invert() = this
}