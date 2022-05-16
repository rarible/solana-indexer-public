package com.rarible.protocol.solana.common.event

import com.rarible.blockchain.scanner.solana.model.SolanaLog
import java.math.BigInteger
import java.time.Instant

sealed class EscrowEvent : EntityEvent {
    abstract val account: String
    abstract val auctionHouse: String
    abstract val wallet: String
    abstract val amount: BigInteger

    abstract val timestamp: Instant
    abstract override val reversed: Boolean
    abstract override val log: SolanaLog
}

data class EscrowBuyEvent(
    override val account: String,
    override val auctionHouse: String,
    override val wallet: String,
    override val amount: BigInteger,
    override val timestamp: Instant,
    override val reversed: Boolean,
    override val log: SolanaLog
) : EscrowEvent()

data class EscrowExecuteSaleEvent(
    override val account: String,
    override val auctionHouse: String,
    override val wallet: String,
    override val amount: BigInteger,
    override val timestamp: Instant,
    override val reversed: Boolean,
    override val log: SolanaLog
) : EscrowEvent()

data class EscrowDepositEvent(
    override val account: String,
    override val auctionHouse: String,
    override val wallet: String,
    override val amount: BigInteger,
    override val timestamp: Instant,
    override val reversed: Boolean,
    override val log: SolanaLog
) : EscrowEvent()

data class EscrowWithdrawEvent(
    override val account: String,
    override val auctionHouse: String,
    override val wallet: String,
    override val amount: BigInteger,
    override val timestamp: Instant,
    override val reversed: Boolean,
    override val log: SolanaLog
) : EscrowEvent()