package com.rarible.protocol.solana.common.records

import com.rarible.blockchain.scanner.solana.model.SolanaLog
import com.rarible.protocol.solana.common.model.Order
import com.rarible.protocol.solana.common.model.TokenAssetType
import java.math.BigInteger
import java.time.Instant

sealed class SolanaAuctionHouseOrderRecord : SolanaBaseLogRecord() {
    abstract val auctionHouse: String
    abstract val orderId: String

    final override fun getKey(): String = orderId

    data class BuyRecord(
        val maker: String,
        val buyPrice: BigInteger,
        val tokenAccount: String,
        val mint: String,
        val amount: BigInteger,
        override val log: SolanaLog,
        override val timestamp: Instant,
        override val auctionHouse: String
    ) : SolanaAuctionHouseOrderRecord() {
        override val orderId: String
            get() = Order.calculateAuctionHouseOrderId(maker, TokenAssetType(mint), auctionHouse)
    }

    data class SellRecord(
        val maker: String,
        val sellPrice: BigInteger,
        // only token account is available in the record.
        // mint will be set in the SolanaBalanceLogEventFilter by account <-> mint association.
        val tokenAccount: String,
        val mint: String,
        val amount: BigInteger,
        override val log: SolanaLog,
        override val timestamp: Instant,
        override val auctionHouse: String
    ) : SolanaAuctionHouseOrderRecord() {
        override val orderId: String
            get() = Order.calculateAuctionHouseOrderId(maker, TokenAssetType(mint), auctionHouse)
    }

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

        override val orderId: String
            get() = when (direction) {
                Direction.BUY -> Order.calculateAuctionHouseOrderId(buyer, TokenAssetType(treasuryMint), auctionHouse)
                Direction.SELL -> Order.calculateAuctionHouseOrderId(seller, TokenAssetType(mint), auctionHouse)
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
    ) : SolanaAuctionHouseOrderRecord() {
        override val orderId: String
            get() = Order.calculateAuctionHouseOrderId(owner, TokenAssetType(mint), auctionHouse)
    }
}