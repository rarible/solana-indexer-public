package com.rarible.protocol.solana.common.model

import com.rarible.core.entity.reducer.model.Entity
import com.rarible.protocol.solana.common.event.TokenEvent
import org.springframework.data.annotation.AccessType
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

typealias TokenId = String

@Document("token")
data class Token(
    val mint: String,
    // TODO: change to BigInteger.
    val supply: Long,
    // TODO: probably, can be calculated based on supply = 0
    // TODO: rename to 'closed'?
    val isDeleted: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
    val metaplexMeta: MetaplexTokenMeta?,
    /**
     * Historical states of [metaplexMeta] values used to revert to a previous state.
     */
    val metaplexMetaHistory: List<MetaplexTokenMeta>,
    override val revertableEvents: List<TokenEvent>
) : Entity<TokenId, TokenEvent, Token> {
    @get:Id
    @get:AccessType(AccessType.Type.PROPERTY)
    override var id: TokenId
        get() = mint
        set(_) {}

    override fun withRevertableEvents(events: List<TokenEvent>): Token =
        copy(revertableEvents = events)

    companion object {
        fun empty(mint: String): Token = Token(
            mint = mint,
            supply = 0L,
            revertableEvents = emptyList(),
            isDeleted = false,
            metaplexMeta = null,
            createdAt = Instant.EPOCH,
            updatedAt = Instant.EPOCH,
            metaplexMetaHistory = emptyList()
        )
    }
}
