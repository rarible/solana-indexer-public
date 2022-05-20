package com.rarible.protocol.solana.common.model

import com.rarible.protocol.solana.common.event.EscrowEvent
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.math.BigInteger
import java.time.Instant

typealias EscrowId = String

@Document(Escrow.COLLECTION)
data class Escrow(
    @Id
    val account: EscrowId,
    val wallet: String,
    val auctionHouse: AuctionHouseId,
    val value: BigInteger,
    val states: List<Escrow>,
    val lastEvent: EscrowEvent?,
    override val revertableEvents: List<EscrowEvent>,
    override val createdAt: Instant?,
    override val updatedAt: Instant
) : SolanaEntity<EscrowId, EscrowEvent, Escrow> {
    override val id: EscrowId get() = account

    override fun withRevertableEvents(events: List<EscrowEvent>): Escrow {
        return copy(revertableEvents = events)
    }

    override fun toString(): String {
        return "Escrow(account='$account', wallet='$wallet', auctionHouse='$auctionHouse', value=$value, createdAt=$createdAt, updatedAt=$updatedAt)"
    }


    companion object {
        const val COLLECTION = "escrow"

        fun empty(): Escrow = Escrow(
            account = "",
            wallet = "",
            auctionHouse = "",
            lastEvent = null,
            value = BigInteger.ZERO,
            createdAt = Instant.EPOCH,
            updatedAt = Instant.EPOCH,
            states = emptyList(),
            revertableEvents = emptyList()
        )
    }
}