package com.rarible.protocol.solana.nft.listener.service.records

import com.rarible.blockchain.scanner.solana.model.SolanaLog
import java.math.BigInteger
import java.time.Instant

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
