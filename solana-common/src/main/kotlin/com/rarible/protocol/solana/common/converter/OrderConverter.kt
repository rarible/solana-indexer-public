package com.rarible.protocol.solana.common.converter

import com.rarible.protocol.solana.common.model.Order
import com.rarible.protocol.solana.common.model.OrderStatus
import com.rarible.protocol.solana.common.model.order.filter.OrdersWithContinuation
import com.rarible.protocol.solana.common.records.OrderDirection
import com.rarible.protocol.solana.common.service.PriceNormalizer
import com.rarible.protocol.solana.dto.AuctionHouseOrderDataV1Dto
import com.rarible.protocol.solana.dto.AuctionHouseOrderDto
import com.rarible.protocol.solana.dto.OrderDto
import com.rarible.protocol.solana.dto.OrderStatusDto
import com.rarible.protocol.solana.dto.OrdersDto
import org.springframework.stereotype.Component

@Component
class OrderConverter(
    private val assetConverter: AssetConverter,
    private val priceNormalizer: PriceNormalizer
) {

    suspend fun convert(order: Order): OrderDto =
        AuctionHouseOrderDto(
            maker = order.maker,
            make = assetConverter.convert(order.make),
            take = assetConverter.convert(order.take),
            fill = when (order.direction) {
                OrderDirection.SELL -> priceNormalizer.normalize(order.make.type, order.fill)
                OrderDirection.BUY -> priceNormalizer.normalize(order.take.type, order.fill)
            },
            start = null,
            end = null,
            createdAt = order.createdAt,
            updatedAt = order.updatedAt,
            hash = order.id,
            status = order.status.toDto(),
            data = AuctionHouseOrderDataV1Dto(auctionHouse = order.auctionHouse)
        )

    suspend fun convert(ordersWithContinuation: OrdersWithContinuation): OrdersDto =
        OrdersDto(
            orders = ordersWithContinuation.orders.map { convert(it) },
            continuation = ordersWithContinuation.continuation
        )

    private fun OrderStatus.toDto() = when (this) {
        OrderStatus.ACTIVE -> OrderStatusDto.ACTIVE
        OrderStatus.CANCELLED -> OrderStatusDto.CANCELLED
        OrderStatus.FILLED -> OrderStatusDto.FILLED
    }
}
