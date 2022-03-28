package com.rarible.protocol.solana.common.records

import com.rarible.blockchain.scanner.solana.model.SolanaLog
import com.rarible.protocol.solana.common.model.Order
import com.rarible.protocol.solana.common.model.WrappedSolAssetType
import com.rarible.protocol.solana.common.model.getAssetType
import java.math.BigInteger
import java.time.Instant

sealed class SolanaAuctionHouseOrderRecord : SolanaBaseLogRecord() {
    abstract val auctionHouse: String
    abstract val mint: String
    abstract val orderId: String

    final override fun getKey(): String = auctionHouse

    data class BuyRecord(
        val maker: String,
        val treasuryMint: String,
        val buyPrice: BigInteger,
        val tokenAccount: String,
        override val mint: String,
        val amount: BigInteger,
        override val log: SolanaLog,
        override val timestamp: Instant,
        override val auctionHouse: String,
        override val orderId: String
    ) : SolanaAuctionHouseOrderRecord() {
        fun withUpdatedOrderId() = copy(
            orderId = Order.calculateAuctionHouseOrderId(
                maker = maker,
                make = getAssetType(treasuryMint),
                auctionHouse = auctionHouse
            )
        )
    }

    data class SellRecord(
        val maker: String,
        val sellPrice: BigInteger,
        val tokenAccount: String,
        override val mint: String,
        val amount: BigInteger,
        override val log: SolanaLog,
        override val timestamp: Instant,
        override val auctionHouse: String,
        override val orderId: String
    ) : SolanaAuctionHouseOrderRecord() {
        fun withUpdatedOrderId() = copy(
            orderId = Order.calculateAuctionHouseOrderId(maker, getAssetType(mint), auctionHouse)
        )
    }

    data class ExecuteSaleRecord(
        val buyer: String,
        val seller: String,
        val price: BigInteger,
        override val mint: String,
        val treasuryMint: String,
        val amount: BigInteger,
        val direction: Direction,
        override val log: SolanaLog,
        override val timestamp: Instant,
        override val auctionHouse: String,
        override val orderId: String = when (direction) {
            Direction.BUY -> Order.calculateAuctionHouseOrderId(buyer, getAssetType(treasuryMint), auctionHouse)
            Direction.SELL -> Order.calculateAuctionHouseOrderId(seller, getAssetType(mint), auctionHouse)
        }
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
        override val mint: String,
        val price: BigInteger,
        val amount: BigInteger,
        override val log: SolanaLog,
        override val timestamp: Instant,
        override val auctionHouse: String,
        // TODO[orders]: this may not work if the auction house was created with a different treasury mint (not wrapped SOL).
        //  The Cancel event does not have 'treasureMint' field. We can save this info in the database by CreateAuctionHouseRecord,
        //  similar how we do for AccountToMintAssociationService.
        override val orderId: String = Order.calculateAuctionHouseOrderId(owner, WrappedSolAssetType, auctionHouse)
    ) : SolanaAuctionHouseOrderRecord()
}