package com.rarible.protocol.solana.common.converter

import com.rarible.protocol.solana.common.model.AssetType
import com.rarible.protocol.solana.common.model.Order
import com.rarible.protocol.solana.common.model.OrderStatus
import com.rarible.protocol.solana.common.model.TokenFtAssetType
import com.rarible.protocol.solana.common.model.TokenNftAssetType
import com.rarible.protocol.solana.common.model.WrappedSolAssetType
import com.rarible.protocol.solana.common.model.order.filter.OrdersWithContinuation
import com.rarible.protocol.solana.dto.AssetDto
import com.rarible.protocol.solana.dto.AssetTypeDto
import com.rarible.protocol.solana.dto.AuctionHouseOrderDto
import com.rarible.protocol.solana.dto.OrderDto
import com.rarible.protocol.solana.dto.OrderStatusDto
import com.rarible.protocol.solana.dto.OrdersDto
import com.rarible.protocol.solana.dto.TokenAssetTypeDto

object OrderConverter {

    fun convert(order: Order): OrderDto =
        AuctionHouseOrderDto(
            maker = order.maker,
            make = AssetDto(convert(order.make.type), order.make.amount.toBigDecimal()),
            take = AssetDto(convert(order.take.type), order.take.amount.toBigDecimal()),
            fill = order.fill,
            start = null,
            end = null,
            createdAt = order.createdAt,
            updatedAt = order.updatedAt,
            hash = order.id,
            status = order.status.toDto()
        )

    fun convert(assetType: AssetType): AssetTypeDto = TokenAssetTypeDto(
        mint = assetType.tokenAddress,
        isNft = when (assetType) {
            is TokenFtAssetType -> false
            is TokenNftAssetType -> true
            WrappedSolAssetType -> false
        },
        isCurrency = when (assetType) {
            is TokenFtAssetType -> true
            is TokenNftAssetType -> false
            WrappedSolAssetType -> true
        }
    )

    fun convert(ordersWithContinuation: OrdersWithContinuation): OrdersDto =
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
