package com.rarible.protocol.solana.common.records

import com.rarible.blockchain.scanner.solana.model.SolanaLog
import java.math.BigInteger
import java.time.Instant

sealed class SolanaAuctionHouseOrderRecord : SolanaBaseLogRecord() {
    abstract val auctionHouse: String

    final override fun getKey(): String = auctionHouse

    data class BuyRecord(
        val maker: String,
        val buyPrice: BigInteger,
        val tokenAccount: String,
        val mint: String,
        val amount: BigInteger,
        override val log: SolanaLog,
        override val timestamp: Instant,
        override val auctionHouse: String
    ) : SolanaAuctionHouseOrderRecord()

    data class SellRecord(
        val maker: String,
        val sellPrice: BigInteger,
        val tokenAccount: String,
        val mint: String,
        val amount: BigInteger,
        override val log: SolanaLog,
        override val timestamp: Instant,
        override val auctionHouse: String
    ) : SolanaAuctionHouseOrderRecord()

    data class ExecuteSaleRecord(
        val buyer: String,
        val seller: String,
        val price: BigInteger,
        val mint: String,
        val treasuryMint: String,
        val amount: BigInteger,
        val direction: Direction,
        override val log: SolanaLog,
        override val timestamp: Instant,
        override val auctionHouse: String
    ) : SolanaAuctionHouseOrderRecord() {
        enum class Direction {
            BUY, SELL
        }

        override val id: String
            get() = super.id + ":" + when (direction) {
                Direction.BUY -> "buy"
                Direction.SELL -> "sell"
            }
    }

    data class CancelRecord(
        val owner: String,
        val mint: String,
        val price: BigInteger,
        val amount: BigInteger,
        override val log: SolanaLog,
        override val timestamp: Instant,
        override val auctionHouse: String
    ) : SolanaAuctionHouseOrderRecord()
}