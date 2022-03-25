package com.rarible.protocol.solana.common.records

import com.rarible.blockchain.scanner.solana.model.SolanaLog
import java.math.BigInteger
import java.time.Instant

sealed class SolanaTokenRecord : SolanaBaseLogRecord() {
    abstract val mint: String

    override fun getKey(): String = mint

    data class CreateMetaRecord(
        override val log: SolanaLog,
        override val timestamp: Instant,
        override val mint: String
    ) : SolanaTokenRecord()

    data class InitializeMintRecord(
        val mintAuthority: String,
        val decimals: Int,
        override val mint: String,
        override val log: SolanaLog,
        override val timestamp: Instant
    ) : SolanaTokenRecord()

    data class MintToRecord(
        val tokenAccount: String,
        val mintAmount: BigInteger,
        override val mint: String,
        override val log: SolanaLog,
        override val timestamp: Instant
    ) : SolanaTokenRecord()

    data class BurnRecord(
        val tokenAccount: String,
        val burnAmount: BigInteger,
        override val mint: String,
        override val log: SolanaLog,
        override val timestamp: Instant
    ) : SolanaTokenRecord()
}
