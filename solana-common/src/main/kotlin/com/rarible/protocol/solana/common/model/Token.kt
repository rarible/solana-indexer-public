package com.rarible.protocol.solana.common.model

import com.rarible.core.entity.reducer.model.Entity
import com.rarible.protocol.solana.common.event.TokenEvent
import org.springframework.data.annotation.AccessType
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

typealias TokenId = String

@Document("token")
data class Token(
    val mint: String,
    val collection: String? = null,
    val supply: Long, // TODO: change to BigInteger.
    val isDeleted: Boolean,
    override val revertableEvents: List<TokenEvent>
) : Entity<TokenId, TokenEvent, Token> {
    @get:Id
    @get:AccessType(AccessType.Type.PROPERTY)
    override var id: TokenId
        get() = mint
        set(_) {}

    override fun withRevertableEvents(events: List<TokenEvent>): Token {
        return copy(revertableEvents = events)
    }

    companion object {
        fun empty(token: String): Token {
            return Token(
                mint = token,
                collection = null,
                supply = 0L,
                revertableEvents = emptyList(),
                isDeleted = false
            )
        }
    }
}
