package com.rarible.protocol.solana.common.records

import com.rarible.blockchain.scanner.solana.model.SolanaLog
import java.math.BigInteger
import java.time.Instant

sealed class SolanaEscrowRecord: SolanaBaseLogRecord() {
    abstract val auctionHouse: String

    data class BuyRecord(
        val escrow: String,
        val wallet: String,
        val buyPrice: BigInteger,
        override val log: SolanaLog,
        override val timestamp: Instant,
        override val auctionHouse: String,
    ) : SolanaEscrowRecord() {
        override fun getKey(): String = auctionHouse
    }

    data class ExecuteSaleRecord(
        val mint: String,
        val escrow: String,
        val wallet: String,
        val buyPrice: BigInteger,
        override val log: SolanaLog,
        override val timestamp: Instant,
        override val auctionHouse: String,
    ) : SolanaEscrowRecord() {
        override fun getKey(): String = auctionHouse
    }

    data class DepositRecord(
        val escrow: String,
        val wallet: String,
        val amount: BigInteger,
        override val log: SolanaLog,
        override val auctionHouse: String,
        override val timestamp: Instant
    ) : SolanaEscrowRecord() {
        override fun getKey(): String = auctionHouse
    }

    data class WithdrawRecord(
        val escrow: String,
        val wallet: String,
        val amount: BigInteger,
        override val log: SolanaLog,
        override val auctionHouse: String,
        override val timestamp: Instant
    ) : SolanaEscrowRecord() {
        override fun getKey(): String = auctionHouse
    }
}