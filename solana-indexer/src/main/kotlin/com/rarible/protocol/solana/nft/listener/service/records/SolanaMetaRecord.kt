package com.rarible.protocol.solana.nft.listener.service.records

import com.rarible.blockchain.scanner.solana.model.SolanaLog
import com.rarible.protocol.solana.borsh.MetaplexCreateMetadataAccount
import com.rarible.protocol.solana.borsh.MetaplexUpdateMetadataAccountArgs
import java.time.Instant

sealed class SolanaMetaRecord : SolanaBaseLogRecord() {
    abstract val metaAccount: String

    override fun getKey(): String = metaAccount

    data class MetaplexCreateMetadataRecord(
        val mint: String,
        val data: MetaplexCreateMetadataAccount,
        override val metaAccount: String,
        override val log: SolanaLog,
        override val timestamp: Instant
    ) : SolanaMetaRecord()

    data class MetaplexUpdateMetadataRecord(
        val mint: String,
        val newData: MetaplexUpdateMetadataAccountArgs,
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
}
