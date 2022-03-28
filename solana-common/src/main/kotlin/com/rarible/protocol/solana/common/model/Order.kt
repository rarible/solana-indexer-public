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
    FILLED
}

enum class OrderType {
    BUY,
    SELL
}

sealed class AssetType {
    abstract val tokenAddress: String
}

object WrappedSolAssetType : AssetType() {
    override val tokenAddress: String = "So11111111111111111111111111111111111111112"
}

data class TokenNftAssetType(
    override val tokenAddress: String,
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
    val take: Asset,
    val fill: BigInteger,
    val createdAt: Instant,
    val updatedAt: Instant,
    override val revertableEvents: List<OrderEvent>,
    @Id
    override val id: String = calculateAuctionHouseOrderId(maker, make.type, auctionHouse),
) : Entity<OrderId, OrderEvent, Order> {

    override fun withRevertableEvents(events: List<OrderEvent>): Order {
        return copy(revertableEvents = events)
    }

    companion object {
        const val COLLECTION = "order"

        fun empty(): Order = Order(
            auctionHouse = "",
            maker = "",
            status = OrderStatus.CANCELLED,
            type = OrderType.BUY,
            make = Asset(TokenNftAssetType(tokenAddress = ""), BigInteger.ZERO),
            take = Asset(TokenNftAssetType(tokenAddress = ""), BigInteger.ZERO),
            fill = BigInteger.ZERO,
            createdAt = Instant.EPOCH,
            updatedAt = Instant.EPOCH,
            revertableEvents = emptyList()
        )

        fun calculateAuctionHouseOrderId(
            maker: String,
            make: AssetType,
            auctionHouse: String
        ): String = Hash.keccak256(maker + make.hash() + auctionHouse)

        private fun AssetType.hash() = tokenAddress
    }
}