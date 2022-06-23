@file:Suppress("UnstableApiUsage")

package com.rarible.protocol.solana.common.service

import com.google.common.cache.CacheBuilder
import com.rarible.protocol.solana.common.configuration.SolanaIndexerProperties
import com.rarible.protocol.solana.common.model.Asset
import com.rarible.protocol.solana.common.model.AssetType
import com.rarible.protocol.solana.common.model.Order
import com.rarible.protocol.solana.common.model.OrderMakeAndTakePrice
import com.rarible.protocol.solana.common.model.TokenFtAssetType
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
    private val tokenRepository: TokenRepository,
    private val solanaIndexerProperties: SolanaIndexerProperties
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
                takePrice = if (normalizedTake.compareTo(BigDecimal.ZERO) == 0)
                    BigDecimal.ZERO
                else
                    normalizedMake / normalizedTake
            )
            OrderDirection.SELL -> OrderMakeAndTakePrice(
                makePrice = if (normalizedMake.compareTo(BigDecimal.ZERO) == 0)
                    BigDecimal.ZERO
                else
                    normalizedTake / normalizedMake,
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
            is TokenFtAssetType -> 0 // TODO potentially there should be collection with such coins/decimals, see ethereum or tokens.json
            is WrappedSolAssetType -> 9
        }

    private suspend fun getTokenDecimals(mint: String): Int {
        val decimals = decimalsCache.getIfPresent(mint)
        if (decimals != null) {
            return decimals
        }
        val token = tokenRepository.findByMint(mint)
        if (token == null) {
            val message = "Unable to fetch 'decimals' of the token mint '$mint' because it is not found"
            if (solanaIndexerProperties.featureFlags.isIndexingFromBeginning) {
                logger.error(message)
            } else {
                logger.info(message)
            }
            return 0
        }
        return token.decimals
    }
}