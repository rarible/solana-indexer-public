package com.rarible.protocol.solana.common.model

import com.rarible.core.entity.reducer.model.Entity
import com.rarible.protocol.solana.common.event.OrderEvent
import com.rarible.protocol.solana.common.hash.Hash
import com.rarible.protocol.solana.common.model.Order.Companion.COLLECTION
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

@Document(COLLECTION)
data class Order(
    val auctionHouse: String,
    val maker: String,
    val status: OrderStatus,
    val type: OrderType,
    val make: Asset,
    val cancelled: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
    override val revertableEvents: List<OrderEvent>,
) : Entity<OrderId, OrderEvent, Order> {

    @Id
    override val id = calculateAuctionHouseOrderId(maker, make.type, auctionHouse)

    override fun withRevertableEvents(events: List<OrderEvent>): Order {
        return copy(revertableEvents = events)
    }

    companion object {
        const val COLLECTION = "order"

        fun empty(): Order = Order(
            auctionHouse = "",
            maker = "",
            status = OrderStatus.ENDED,
            type = OrderType.BUY,
            make = Asset(TokenAssetType(tokenAddress = ""), BigInteger.ZERO),
            cancelled = false,
            createdAt = Instant.EPOCH,
            updatedAt = Instant.EPOCH,
            revertableEvents = emptyList()
        )

        // hash
        // ACTIVE
        // FILLED
        // ACTIVE

        fun calculateAuctionHouseOrderId(
            maker: String,
            make: AssetType,
            auctionHouse: String
        ): String = Hash.keccak256(maker + make.hash() + auctionHouse)

        private fun AssetType.hash() = when (this) {
            is TokenAssetType -> tokenAddress
        }
    }
}