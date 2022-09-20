package com.rarible.protocol.solana.common.model

import com.rarible.protocol.solana.common.event.TokenEvent
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.mongodb.core.mapping.Document
import java.math.BigInteger
import java.time.Instant

typealias TokenId = String

@Document(Token.COLLECTION)
data class Token(
    @Id
    val mint: String,
    val supply: BigInteger,
    val decimals: Int,
    override val createdAt: Instant,
    override val updatedAt: Instant,
    override val revertableEvents: List<TokenEvent>,
    @Version
    override val version: Long? = null
) : SolanaEntity<TokenId, TokenEvent, Token> {

    override val id: TokenId get() = mint

    override fun withRevertableEvents(events: List<TokenEvent>): Token =
        copy(revertableEvents = events)

    override fun toString(): String =
        "Token(mint='$mint', supply=$supply, decimals=$decimals, createdAt=$createdAt, updatedAt=$updatedAt)"

    companion object {

        const val COLLECTION = "token"

        fun empty(mint: String): Token = Token(
            mint = mint,
            supply = BigInteger.ZERO,
            revertableEvents = emptyList(),
            decimals = 0,
            createdAt = Instant.EPOCH,
            updatedAt = Instant.EPOCH
        )
    }
}
