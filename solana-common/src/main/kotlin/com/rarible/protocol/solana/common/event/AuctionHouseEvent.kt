package com.rarible.protocol.solana.common.event

import com.rarible.blockchain.scanner.solana.model.SolanaLog
import java.time.Instant

sealed class AuctionHouseEvent : EntityEvent {
    abstract val auctionHouse: String

    abstract val timestamp: Instant
    abstract override val reversed: Boolean
    abstract override val log: SolanaLog
}

data class CreateAuctionHouseEvent(
    val sellerFeeBasisPoints: Int,
    val requiresSignOff: Boolean,
    override val log: SolanaLog,
    override val reversed: Boolean,
    override val auctionHouse: String,
    override val timestamp: Instant
) : AuctionHouseEvent()

data class UpdateAuctionHouseEvent(
    val sellerFeeBasisPoints: Int?,
    val requiresSignOff: Boolean?,
    override val auctionHouse: String,
    override val timestamp: Instant,
    override val reversed: Boolean,
    override val log: SolanaLog
) : AuctionHouseEvent()

