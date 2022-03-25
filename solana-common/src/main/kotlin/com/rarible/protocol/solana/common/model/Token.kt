package com.rarible.protocol.solana.common.model

import com.rarible.core.entity.reducer.model.Entity
import com.rarible.protocol.solana.common.event.TokenEvent
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.math.BigInteger
import java.time.Instant

typealias TokenId = String

@Document("token")
data class Token(
    @Id
    val mint: String,
    val supply: BigInteger,
    // TODO: probably, can be calculated based on supply = 0
    // TODO: rename to 'closed'?
    val decimals: Int,
    val hasMeta: Boolean?,
    val isDeleted: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
    override val revertableEvents: List<TokenEvent>
) : Entity<TokenId, TokenEvent, Token> {

    override val id: TokenId get() = mint

    override fun withRevertableEvents(events: List<TokenEvent>): Token =
        copy(revertableEvents = events)

    companion object {
        fun empty(mint: String): Token = Token(
            mint = mint,
            hasMeta = false,
            supply = BigInteger.ZERO,
            revertableEvents = emptyList(),
            isDeleted = false,
            decimals = 0,
            createdAt = Instant.EPOCH,
            updatedAt = Instant.EPOCH
        )
    }
}
