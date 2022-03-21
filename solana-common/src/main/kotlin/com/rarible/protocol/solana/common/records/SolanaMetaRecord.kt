package com.rarible.protocol.solana.common.records

import com.rarible.blockchain.scanner.solana.model.SolanaLog
import com.rarible.protocol.solana.common.model.MetaplexMetaFields
import java.time.Instant

sealed class SolanaMetaRecord : SolanaBaseLogRecord() {
    abstract val metaAccount: String

    override fun getKey(): String = metaAccount

    data class MetaplexCreateMetadataAccountRecord(
        val mint: String,
        val meta: MetaplexMetaFields,
        val mutable: Boolean,
        override val metaAccount: String,
        override val log: SolanaLog,
        override val timestamp: Instant
    ) : SolanaMetaRecord()

    data class MetaplexUpdateMetadataRecord(
        val mint: String,
        val updatedMeta: MetaplexMetaFields?,
        val updatedMutable: Boolean?,
        val updateAuthority: String?,
        val primarySaleHappened: Boolean?,
        override val metaAccount: String,
        override val log: SolanaLog,
        override val timestamp: Instant
    ) : SolanaMetaRecord()

    data class MetaplexVerifyCollectionRecord(
        val collectionAccount: String,
        override val metaAccount: String,
        override val log: SolanaLog,
        override val timestamp: Instant
    ) : SolanaMetaRecord()

    data class MetaplexUnVerifyCollectionRecord(
        val unVerifyCollectionAccount: String,
        override val metaAccount: String,
        override val log: SolanaLog,
        override val timestamp: Instant
    ) : SolanaMetaRecord()

    data class MetaplexSignMetadataRecord(
        val creatorAddress: String,
        override val metaAccount: String,
        override val log: SolanaLog,
        override val timestamp: Instant
    ) : SolanaMetaRecord()

    data class SetAndVerifyMetadataRecord(
        val mint: String,
        override val metaAccount: String,
        override val log: SolanaLog,
        override val timestamp: Instant
    ) : SolanaMetaRecord()
}
