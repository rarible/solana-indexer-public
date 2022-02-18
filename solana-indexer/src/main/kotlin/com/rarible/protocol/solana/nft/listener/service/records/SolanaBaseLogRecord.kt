package com.rarible.protocol.solana.nft.listener.service.records

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.rarible.blockchain.scanner.solana.model.SolanaLog
import com.rarible.blockchain.scanner.solana.model.SolanaLogRecord
import com.rarible.protocol.solana.borsh.MetaplexCreateMetadataAccount
import com.rarible.protocol.solana.borsh.MetaplexUpdateMetadataAccountArgs
import java.time.Instant

@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
sealed class SolanaBaseLogRecord : SolanaLogRecord() {
    abstract val timestamp: Instant
}

sealed class SolanaTokenRecord : SolanaBaseLogRecord() {
    abstract val mint: String

    override fun getKey(): String = mint

    data class InitializeMintRecord(
        val mintAuthority: String,
        val decimals: Int,
        override val mint: String,
        override val log: SolanaLog,
        override val timestamp: Instant
    ) : SolanaTokenRecord()

    data class InitializeTokenAccountRecord(
        val tokenAccount: String,
        val owner: String,
        override val mint: String,
        override val log: SolanaLog,
        override val timestamp: Instant
    ) : SolanaTokenRecord()

    data class MintToRecord(
        val tokenAccount: String,
        val mintAmount: Long,
        override val mint: String,
        override val log: SolanaLog,
        override val timestamp: Instant
    ) : SolanaTokenRecord()

    data class BurnRecord(
        val tokenAccount: String,
        val burnAmount: Long,
        override val mint: String,
        override val log: SolanaLog,
        override val timestamp: Instant
    ) : SolanaTokenRecord()
}

sealed class SolanaBalanceRecord : SolanaBaseLogRecord() {
    abstract val account: String

    override fun getKey(): String = account

    data class MintToRecord(
        val mintAmount: Long,
        val mint: String,
        override val account: String,
        override val log: SolanaLog,
        override val timestamp: Instant
    ) : SolanaBalanceRecord()

    data class BurnRecord(
        val mint: String,
        val burnAmount: Long,
        override val account: String,
        override val log: SolanaLog,
        override val timestamp: Instant
    ) : SolanaBalanceRecord()

    data class TransferIncomeRecord(
        val from: String,
        val to: String,
        val incomeAmount: Long,
        override val log: SolanaLog,
        override val timestamp: Instant
    ) : SolanaBalanceRecord() {
        override val account: String
            get() = to
    }

    data class TransferOutcomeRecord(
        val from: String,
        val to: String,
        val outcomeAmount: Long,
        override val log: SolanaLog,
        override val timestamp: Instant
    ) : SolanaBalanceRecord() {
        override val account: String
            get() = from
    }
}

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
