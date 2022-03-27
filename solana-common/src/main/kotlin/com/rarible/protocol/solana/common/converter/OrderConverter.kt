package com.rarible.protocol.solana.common.converter

import com.rarible.protocol.solana.common.model.Asset
import com.rarible.protocol.solana.common.model.AssetType
import com.rarible.protocol.solana.common.model.Order
import com.rarible.protocol.solana.common.model.OrderStatus
import com.rarible.protocol.solana.common.model.TokenAssetType
import com.rarible.protocol.solana.dto.AssetDto
import com.rarible.protocol.solana.dto.AssetTypeDto
import com.rarible.protocol.solana.dto.AuctionHouseOrderDto
import com.rarible.protocol.solana.dto.OrderDto
import com.rarible.protocol.solana.dto.OrderStatusDto
import com.rarible.protocol.solana.dto.TokenAssetTypeDto
import java.math.BigInteger

object OrderConverter {

    fun convert(source: Order): OrderDto {
        return AuctionHouseOrderDto(
            hash = source.id,
            maker = source.maker,
            make = convert(source.make),
            take = convert(source.make), // TODO take should be
            fill = BigInteger.ZERO, // TODO ???
            start = null,
            end = null,
            createdAt = source.createdAt,
            updatedAt = source.updatedAt,
            status = convert(source.status)
        )
    }

    private fun convert(source: OrderStatus): OrderStatusDto {
        return when (source) {
            OrderStatus.ACTIVE -> OrderStatusDto.ACTIVE
            OrderStatus.CANCELLED -> OrderStatusDto.CANCELLED
            OrderStatus.ENDED -> OrderStatusDto.FILLED // TODO ???
        }
    }

    private fun convert(source: Asset): AssetDto {
        return AssetDto(
            type = convert(source.type),
            value = source.amount.toBigDecimal()
        )
    }

    private fun convert(source: AssetType): AssetTypeDto {
        return when (source) {
            is TokenAssetType -> TokenAssetTypeDto(
                mint = source.tokenAddress,
                isNft = true, // TODO ???
                isCurrency = false // TODO ???
            )
        }
    }

}