package com.rarible.protocol.solana.nft.listener.service.records

import com.rarible.blockchain.scanner.solana.model.SolanaLog
import com.rarible.protocol.solana.borsh.MetaplexCreateMetadataAccountArgs
import com.rarible.protocol.solana.borsh.MetaplexUpdateMetadataAccountArgs
import java.time.Instant

sealed class SolanaMetaRecord : SolanaBaseLogRecord() {
    abstract val metaAccount: String

    override fun getKey(): String = metaAccount

    data class MetaplexCreateMetadataAccountRecord(
        val mint: String,
        val data: MetaplexCreateMetadataAccountArgs,
        override val metaAccount: String,
        override val log: SolanaLog,
        override val timestamp: Instant
    ) : SolanaMetaRecord()

    data class MetaplexUpdateMetadataRecord(
        val mint: String,
        val updateArgs: MetaplexUpdateMetadataAccountArgs,
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

    // TODO: add SetAndVerifyCollection instruction
}
