package com.rarible.protocol.solana.common.model

import com.rarible.protocol.solana.common.event.BalanceEvent
import org.springframework.data.annotation.Id
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
    val lastEvent: BalanceEvent?,
    override val createdAt: Instant,
    override val updatedAt: Instant,
    override val revertableEvents: List<BalanceEvent>,
    override val version: Long? = null
) : SolanaEntity<BalanceId, BalanceEvent, Balance> {

    override val id: BalanceId get() = account

    override fun withRevertableEvents(events: List<BalanceEvent>): Balance =
        copy(revertableEvents = events)

    override fun toString(): String =
        "Balance(account='$account', value=$value, mint='$mint', owner='$owner', createdAt=$createdAt, updatedAt=$updatedAt)"

    companion object {
        const val COLLECTION = "balance"

        fun empty(account: String, version: Long? = null): Balance = Balance(
            account = account,
            owner = "",
            mint = "",
            lastEvent = null,
            value = BigInteger.ZERO,
            createdAt = Instant.EPOCH,
            updatedAt = Instant.EPOCH,
            revertableEvents = emptyList(),
            version = version
        )
    }
}
