package com.rarible.protocol.solana.common.records

import com.rarible.blockchain.scanner.solana.model.SolanaLog
import java.math.BigInteger
import java.time.Instant

sealed class SolanaBalanceRecord : SolanaBaseLogRecord() {
    abstract val account: String

    override fun getKey(): String = account

    data class InitializeBalanceAccountRecord(
        val balanceAccount: String,
        val owner: String,
        val mint: String,
        override val log: SolanaLog,
        override val timestamp: Instant
    ) : SolanaBalanceRecord() {
        override val account: String get() = balanceAccount
    }

    data class MintToRecord(
        val mintAmount: BigInteger,
        val mint: String,
        override val account: String,
        override val log: SolanaLog,
        override val timestamp: Instant
    ) : SolanaBalanceRecord()

    data class BurnRecord(
        val mint: String,
        val burnAmount: BigInteger,
        override val account: String,
        override val log: SolanaLog,
        override val timestamp: Instant
    ) : SolanaBalanceRecord()

    data class TransferIncomeRecord(
        val from: String,
        val to: String,
        val mint: String?,
        val incomeAmount: BigInteger,
        override val log: SolanaLog,
        override val timestamp: Instant
    ) : SolanaBalanceRecord() {
        override val account: String
            get() = to

        /**
         * Append ':income' to distinguish this
         * log from [TransferOutcomeRecord] having the same [log].
         */
        override val id: String get() = super.id + ":income"
    }

    data class TransferOutcomeRecord(
        val from: String,
        val to: String,
        val mint: String?,
        val outcomeAmount: BigInteger,
        override val log: SolanaLog,
        override val timestamp: Instant
    ) : SolanaBalanceRecord() {
        override val account: String
            get() = from

        /**
         * Append ':outcome' to distinguish this
         * log from [TransferIncomeRecord] having the same [log].
         */
        override val id: String get() = super.id + ":outcome"
    }
}
