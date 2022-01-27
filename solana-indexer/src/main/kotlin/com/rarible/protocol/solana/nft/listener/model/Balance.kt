package com.rarible.protocol.solana.nft.listener.model

import com.rarible.core.entity.reducer.model.Entity
import com.rarible.protocol.solana.nft.listener.service.balance.BalanceEvent
import org.springframework.data.annotation.AccessType
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.mongodb.core.mapping.Document

typealias BalanceId = String

@Document("balance")
data class Balance(
    val account: String,
    val value: Long,
    override val revertableEvents: List<BalanceEvent> = emptyList()
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
        fun empty(owner: String): Balance {
            return Balance(
                account = owner,
                value = 0L
            )
        }
    }
}