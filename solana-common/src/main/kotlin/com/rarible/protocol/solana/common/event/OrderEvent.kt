package com.rarible.protocol.solana.common.event

import com.rarible.blockchain.scanner.solana.model.SolanaLog
import java.math.BigInteger
import java.time.Instant

sealed class OrderEvent : EntityEvent {
    abstract val maker: String
    abstract val mint: String

    abstract val timestamp: Instant
    abstract override val reversed: Boolean
    abstract override val log: SolanaLog
    open fun invert() : OrderEvent? = null
}

data class OrderBuyEvent(
    override val maker: String,
    val buyPrice: BigInteger,
    override val mint: String,
    val amount: BigInteger,
    override val timestamp: Instant,
    override val reversed: Boolean,
    override val log: SolanaLog
) : OrderEvent()

data class OrderSellEvent(
    override val maker: String,
    val sellPrice: BigInteger,
    override val mint: String,
    val amount: BigInteger,
    override val timestamp: Instant,
    override val reversed: Boolean,
    override val log: SolanaLog
) : OrderEvent()

data class ExecuteSaleEvent(
    override val maker: String,
    val price: BigInteger,
    override val mint: String,
    val amount: BigInteger,
    val direction: Direction,
    override val timestamp: Instant,
    override val reversed: Boolean,
    override val log: SolanaLog
) : OrderEvent() {
    enum class Direction {
        BUY, SELL
    }
}