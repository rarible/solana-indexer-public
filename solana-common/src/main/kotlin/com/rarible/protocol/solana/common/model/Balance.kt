package com.rarible.protocol.solana.common.model

import com.rarible.core.entity.reducer.model.Entity
import com.rarible.protocol.solana.common.event.BalanceEvent
import org.springframework.data.annotation.AccessType
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.mongodb.core.mapping.Document
import java.math.BigInteger
import java.time.Instant

typealias BalanceId = String

@Document(Balance.COLLECTION)
data class Balance(
    @Id
    val account: BalanceId,
    val owner: String,
    val mint: String,
    val value: BigInteger,
    val createdAt: Instant,
    val updatedAt: Instant,
    override val revertableEvents: List<BalanceEvent>
) : Entity<BalanceId, BalanceEvent, Balance> {

    override val id: BalanceId get() = account

    override fun withRevertableEvents(events: List<BalanceEvent>): Balance =
        copy(revertableEvents = events)

    companion object {
        const val COLLECTION = "balance"

        fun empty(account: String): Balance = Balance(
            account = account,
            owner = "",
            mint = "",
            value = BigInteger.ZERO,
            createdAt = Instant.EPOCH,
            updatedAt = Instant.EPOCH,
            revertableEvents = emptyList()
        )
    }
}
