package com.rarible.protocol.solana.common.model

import com.rarible.core.entity.reducer.model.Entity
import com.rarible.protocol.solana.common.event.OrderEvent
import com.rarible.protocol.solana.common.hash.Hash
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.math.BigInteger
import java.time.Instant

typealias OrderId = String

enum class OrderStatus {
    ACTIVE,
    CANCELLED,
    ENDED
}

enum class OrderType {
    BUY,
    SELL
}

sealed class AssetType

data class TokenAssetType(
    val tokenAddress: String
) : AssetType()

data class Asset(
    val type: AssetType,
    val amount: BigInteger
)

@Document("token")
data class Order(
    val maker: String,
    val status: OrderStatus,
    val salt: String,
    val type: OrderType,
    val make: Asset,
    val take: Asset,
    val cancelled: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
    override val revertableEvents: List<OrderEvent>,
) : Entity<OrderId, OrderEvent, Order> {
    @Id
    override val id = Hash.keccak256(maker + make + take + salt)

    override fun withRevertableEvents(events: List<OrderEvent>): Order {
        return copy(revertableEvents = events)
    }
}