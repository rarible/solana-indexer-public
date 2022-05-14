package com.rarible.protocol.solana.common.model

import com.rarible.protocol.solana.common.event.AuctionHouseEvent
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

typealias AuctionHouseId = String

@Document(AuctionHouse.COLLECTION)
data class AuctionHouse(
    @Id
    val account: AuctionHouseId,
    val sellerFeeBasisPoints: Int,
    val requiresSignOff: Boolean,
    override val revertableEvents: List<AuctionHouseEvent>,
    override val createdAt: Instant,
    override val updatedAt: Instant
) : SolanaEntity<AuctionHouseId, AuctionHouseEvent, AuctionHouse> {
    override val id: AuctionHouseId get() = account

    override fun withRevertableEvents(events: List<AuctionHouseEvent>): AuctionHouse =
        copy(revertableEvents = events)

    override fun toString(): String {
        return "AuctionHouse(account='$account', sellerFeeBasisPoints=$sellerFeeBasisPoints, requiresSignOff=$requiresSignOff, createdAt=$createdAt, updatedAt=$updatedAt, id='$id')"
    }

    companion object {
        const val COLLECTION = "auction-house"

        fun empty(auctionHouse: String): AuctionHouse = AuctionHouse(
            account = auctionHouse,
            sellerFeeBasisPoints = 0,
            requiresSignOff = false,
            revertableEvents = emptyList(),
            createdAt = Instant.EPOCH,
            updatedAt = Instant.EPOCH
        )
    }
}
