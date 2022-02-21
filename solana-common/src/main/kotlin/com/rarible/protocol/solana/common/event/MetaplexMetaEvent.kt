package com.rarible.protocol.solana.common.event

import com.rarible.blockchain.scanner.solana.model.SolanaLog
import com.rarible.protocol.solana.common.model.MetaplexMetaFields
import java.time.Instant

sealed class MetaplexMetaEvent : EntityEvent {
    abstract val timestamp: Instant
    abstract val metaAddress: String
}

data class MetaplexCreateMetadataAccountEvent(
    override val log: SolanaLog,
    override val reversed: Boolean,
    override val metaAddress: String,
    override val timestamp: Instant,
    val token: String,
    val metadata: MetaplexMetaFields,
    val isMutable: Boolean
) : MetaplexMetaEvent()

data class MetaplexUpdateMetadataEvent(
    override val log: SolanaLog,
    override val reversed: Boolean,
    override val metaAddress: String,
    override val timestamp: Instant,
    val newMetadata: MetaplexMetaFields?,
    val newIsMutable: Boolean?
) : MetaplexMetaEvent()

data class MetaplexVerifyCollectionMetadataEvent(
    override val log: SolanaLog,
    override val reversed: Boolean,
    override val metaAddress: String,
    override val timestamp: Instant
) : MetaplexMetaEvent()

data class MetaplexUnVerifyCollectionMetadataEvent(
    override val log: SolanaLog,
    override val reversed: Boolean,
    override val metaAddress: String,
    override val timestamp: Instant
) : MetaplexMetaEvent()
