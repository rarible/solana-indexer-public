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

@Document("balance")
data class Balance(
    val account: String,
    val owner: String,
    val mint: String,
    val value: BigInteger,
    val createdAt: Instant,
    val updatedAt: Instant,
    override val revertableEvents: List<BalanceEvent>
) : Entity<BalanceId, BalanceEvent, Balance> {

    @Transient
    private val _id: BalanceId = account

    @get:Id
    @get:AccessType(AccessType.Type.PROPERTY)
    override var id: BalanceId
        get() = _id
        set(_) {}

    override fun withRevertableEvents(events: List<BalanceEvent>): Balance {
        return copy(revertableEvents = events)
    }

    companion object {
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
