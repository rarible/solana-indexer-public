package com.rarible.protocol.solana.common.model

import com.rarible.core.entity.reducer.model.Entity
import com.rarible.protocol.solana.common.event.OrderEvent
import com.rarible.protocol.solana.common.hash.Hash
import com.rarible.protocol.solana.common.model.Order.Companion.COLLECTION
import com.rarible.protocol.solana.common.records.OrderDirection
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.math.BigDecimal
import java.math.BigInteger
import java.time.Instant

typealias OrderId = String

enum class OrderStatus {
    ACTIVE,
    CANCELLED,
    FILLED
}

data class Asset(
    val type: AssetType,
    val amount: BigInteger
)

@Document(Order.COLLECTION)
data class Order(
    val auctionHouse: String,
    val maker: String,
    val status: OrderStatus,
    val make: Asset,
    val take: Asset,
    val makePrice: BigDecimal?,
    val takePrice: BigDecimal?,
    val fill: BigInteger,
    val createdAt: Instant,
    val updatedAt: Instant,
    val direction: OrderDirection,
    override val revertableEvents: List<OrderEvent>,
    @Id
    override val id: String = calculateAuctionHouseOrderId(
        maker = maker,
        mint = when (direction) {
            OrderDirection.BUY -> take.type.tokenAddress
            OrderDirection.SELL -> make.type.tokenAddress
        },
        direction = direction,
        auctionHouse = auctionHouse
    ),
) : Entity<OrderId, OrderEvent, Order> {

    override fun withRevertableEvents(events: List<OrderEvent>): Order = copy(revertableEvents = events)

    companion object {
        const val COLLECTION = "order"

        fun empty(): Order = Order(
            auctionHouse = "",
            maker = "",
            status = OrderStatus.CANCELLED,
            make = Asset(TokenNftAssetType(tokenAddress = ""), BigInteger.ZERO),
            take = Asset(TokenNftAssetType(tokenAddress = ""), BigInteger.ZERO),
            makePrice = BigDecimal.ZERO,
            takePrice = BigDecimal.ZERO,
            fill = BigInteger.ZERO,
            createdAt = Instant.EPOCH,
            updatedAt = Instant.EPOCH,
            revertableEvents = emptyList(),
            direction = OrderDirection.SELL
        )

        fun calculateAuctionHouseOrderId(
            maker: String,
            mint: String,
            direction: OrderDirection,
            auctionHouse: String
        ): String = Hash.keccak256(maker + mint + direction.name + auctionHouse)
    }
}