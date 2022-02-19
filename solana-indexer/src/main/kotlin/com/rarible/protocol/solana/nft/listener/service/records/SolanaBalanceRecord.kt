package com.rarible.protocol.solana.nft.listener.service.records

import com.rarible.blockchain.scanner.solana.model.SolanaLog
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
