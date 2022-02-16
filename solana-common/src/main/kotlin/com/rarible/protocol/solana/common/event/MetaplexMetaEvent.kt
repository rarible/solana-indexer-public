package com.rarible.protocol.solana.common.event

import com.rarible.blockchain.scanner.solana.model.SolanaLog
import com.rarible.protocol.solana.common.model.MetaplexMetaData
import java.time.Instant

sealed class MetaplexMetaEvent : EntityEvent {
    abstract val timestamp: Instant
    abstract val metaAddress: String
}

data class MetaplexCreateMetadataEvent(
    override val log: SolanaLog,
    override val reversed: Boolean,
    override val metaAddress: String,
    override val timestamp: Instant,
    val token: String,
    val metadata: MetaplexMetaData
) : MetaplexMetaEvent()

data class MetaplexVerifyMetadataEvent(
    override val log: SolanaLog,
    override val reversed: Boolean,
    override val metaAddress: String,
    override val timestamp: Instant
) : MetaplexMetaEvent()
