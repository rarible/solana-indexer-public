package com.rarible.protocol.solana.common.model

import com.rarible.core.entity.reducer.model.Entity
import com.rarible.protocol.solana.common.event.TokenEvent
import org.springframework.data.annotation.AccessType
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

typealias TokenId = String

@Document("item")
data class Token(
    val token: String,
    val collection: String? = null,
    val supply: Long, // TODO: change to BigInteger.
    override val revertableEvents: List<TokenEvent> = emptyList(),
) : Entity<TokenId, TokenEvent, Token> {
    @get:Id
    @get:AccessType(AccessType.Type.PROPERTY)
    override var id: TokenId
        get() = token
        set(_) {}

    override fun withRevertableEvents(events: List<TokenEvent>): Token {
        return copy(revertableEvents = events)
    }

    companion object {
        fun empty(token: String): Token {
            return Token(
                token = token,
                collection = null,
                supply = 0L,
                revertableEvents = emptyList()
            )
        }
    }
}
