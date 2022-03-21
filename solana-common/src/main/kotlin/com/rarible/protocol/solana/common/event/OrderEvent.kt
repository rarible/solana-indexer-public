package com.rarible.protocol.solana.common.event

import com.rarible.blockchain.scanner.solana.model.SolanaLog
import java.math.BigInteger
import java.time.Instant

sealed class OrderEvent : EntityEvent {
    abstract val timestamp: Instant
    abstract override val reversed: Boolean
    abstract override val log: SolanaLog
    open fun invert() : OrderEvent? = null
}

data class OrderBuyEvent(
    val maker: String,
    val buyPrice: BigInteger,
    val mint: String,
    val amount: BigInteger,
    override val timestamp: Instant,
    override val reversed: Boolean,
    override val log: SolanaLog
) : OrderEvent()

data class OrderSellEvent(
    val maker: String,
    val sellPrice: BigInteger,
    val mint: String,
    val amount: BigInteger,
    override val timestamp: Instant,
    override val reversed: Boolean,
    override val log: SolanaLog
) : OrderEvent()

data class ExecuteSaleEvent(
    val buyPrice: BigInteger,
    val mint: String,
    val amount: BigInteger,
    override val timestamp: Instant,
    override val reversed: Boolean,
    override val log: SolanaLog
) : OrderEvent()