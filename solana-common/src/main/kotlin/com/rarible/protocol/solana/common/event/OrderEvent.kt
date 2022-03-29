package com.rarible.protocol.solana.common.event

import com.rarible.blockchain.scanner.solana.model.SolanaLog
import com.rarible.protocol.solana.common.model.Asset
import com.rarible.protocol.solana.common.records.OrderDirection
import java.math.BigInteger
import java.time.Instant

sealed class OrderEvent : EntityEvent {
    abstract val auctionHouse: String

    abstract val timestamp: Instant
    abstract override val reversed: Boolean
    abstract override val log: SolanaLog
    open fun invert(): OrderEvent? = null
}

data class OrderBuyEvent(
    override val auctionHouse: String,
    val maker: String,
    val buyPrice: BigInteger,
    val buyAsset: Asset,
    override val timestamp: Instant,
    override val reversed: Boolean,
    override val log: SolanaLog
) : OrderEvent()

data class OrderSellEvent(
    override val auctionHouse: String,
    val maker: String,
    val sellAsset: Asset,
    val sellPrice: BigInteger,
    override val timestamp: Instant,
    override val reversed: Boolean,
    override val log: SolanaLog
) : OrderEvent()

data class OrderCancelEvent(
    val maker: String,
    val mint: String,
    val price: BigInteger,
    val amount: BigInteger,
    val direction: OrderDirection,
    override val log: SolanaLog,
    override val timestamp: Instant,
    override val auctionHouse: String,
    override val reversed: Boolean,
) : OrderEvent()

data class ExecuteSaleEvent(
    override val auctionHouse: String,
    val buyer: String,
    val seller: String,
    val price: BigInteger,
    val mint: String,
    val amount: BigInteger,
    val direction: OrderDirection,
    override val timestamp: Instant,
    override val reversed: Boolean,
    override val log: SolanaLog
) : OrderEvent()