@file:Suppress("UnstableApiUsage")

package com.rarible.protocol.solana.common.service

import com.google.common.cache.CacheBuilder
import com.rarible.protocol.solana.common.model.Asset
import com.rarible.protocol.solana.common.model.AssetType
import com.rarible.protocol.solana.common.model.Order
import com.rarible.protocol.solana.common.model.OrderMakeAndTakePrice
import com.rarible.protocol.solana.common.model.TokenNftAssetType
import com.rarible.protocol.solana.common.model.WrappedSolAssetType
import com.rarible.protocol.solana.common.records.OrderDirection
import com.rarible.protocol.solana.common.repository.TokenRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.BigInteger

@Component
class PriceNormalizer(
    private val tokenRepository: TokenRepository
) {
    private val logger = LoggerFactory.getLogger(PriceNormalizer::class.java)

    private val decimalsCache = CacheBuilder.newBuilder()
        .maximumSize(1024)
        .build<String, Int>()

    suspend fun normalize(asset: Asset): BigDecimal =
        normalize(asset.type, asset.amount)

    suspend fun normalize(assetType: AssetType, value: BigInteger): BigDecimal =
        value.toBigDecimal(getDecimals(assetType))

    suspend fun calculateMakeAndTakePrice(
        make: Asset,
        take: Asset,
        direction: OrderDirection
    ): OrderMakeAndTakePrice {
        val normalizedMake = normalize(make)
        val normalizedTake = normalize(take)
        return when (direction) {
            OrderDirection.BUY -> OrderMakeAndTakePrice(
                makePrice = null,
                takePrice = normalizedMake / normalizedTake
            )
            OrderDirection.SELL -> OrderMakeAndTakePrice(
                makePrice = normalizedTake / normalizedMake,
                takePrice = null
            )
        }
    }

    suspend fun withUpdatedMakeAndTakePrice(order: Order): Order {
        val (makePrice, takePrice) = calculateMakeAndTakePrice(order.make, order.take, order.direction)
        return order.copy(
            makePrice = makePrice,
            takePrice = takePrice
        )
    }

    private suspend fun getDecimals(assetType: AssetType): Int =
        when (assetType) {
            is TokenNftAssetType -> getTokenDecimals(assetType.tokenAddress)
            WrappedSolAssetType -> 9
        }

    private suspend fun getTokenDecimals(mint: String): Int {
        val decimals = decimalsCache.getIfPresent(mint)
        if (decimals != null) {
            return decimals
        }
        val token = tokenRepository.findByMint(mint)
        if (token == null) {
            logger.error("Unable to fetch 'decimals' of the token mint $mint because it is not found")
            return 0
        }
        return token.decimals
    }
}