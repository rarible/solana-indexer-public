package com.rarible.protocol.solana.test

import com.rarible.blockchain.scanner.solana.model.SolanaLog
import com.rarible.core.test.data.randomBigInt
import com.rarible.core.test.data.randomLong
import com.rarible.core.test.data.randomString
import com.rarible.protocol.solana.common.records.OrderDirection
import com.rarible.protocol.solana.common.records.SolanaAuctionHouseOrderRecord
import java.math.BigInteger
import java.time.Instant
import java.time.ZonedDateTime

object OrderRecordDataFactory {

    fun randomBuyRecord(
        maker: String = randomAccount(),
        treasuryMint: String = randomMint(),
        buyPrice: BigInteger = randomBigInt(),
        tokenAccount: String = randomAccount(),
        mint: String = randomMint(),
        amount: BigInteger = randomBigInt(),
        log: SolanaLog = randomSolanaLog(),
        timestamp: Instant = baseTimestamp.plusSeconds(randomLong(86400)),
        auctionHouse: String = randomAccount(),
        orderId: String = randomString(),
    ) = SolanaAuctionHouseOrderRecord.BuyRecord(
        maker = maker,
        treasuryMint = treasuryMint,
        buyPrice = buyPrice,
        tokenAccount = tokenAccount,
        mint = mint,
        amount = amount,
        log = log,
        timestamp = timestamp,
        auctionHouse = auctionHouse,
        orderId = orderId,
    )

    fun randomSellRecord(
        maker: String = randomAccount(),
        sellPrice: BigInteger = randomBigInt(),
        tokenAccount: String = randomAccount(),
        mint: String = randomMint(),
        amount: BigInteger = randomBigInt(),
        log: SolanaLog = randomSolanaLog(),
        timestamp: Instant = baseTimestamp.plusSeconds(randomLong(86400)),
        auctionHouse: String = randomAccount(),
        orderId: String = randomString(),
    ) = SolanaAuctionHouseOrderRecord.SellRecord(
        maker = maker,
        sellPrice = sellPrice,
        tokenAccount = tokenAccount,
        mint = mint,
        amount = amount,
        log = log,
        timestamp = timestamp,
        auctionHouse = auctionHouse,
        orderId = orderId,
    )

    fun randomExecuteSaleRecord(
        buyer: String = randomAccount(),
        seller: String = randomAccount(),
        price: BigInteger = randomBigInt(),
        mint: String = randomMint(),
        treasuryMint: String = randomMint(),
        amount: BigInteger = randomBigInt(),
        direction: OrderDirection = randomDirection(),
        log: SolanaLog = randomSolanaLog(),
        timestamp: Instant = baseTimestamp.plusSeconds(randomLong(86400)),
        auctionHouse: String = randomAccount(),
        orderId: String = randomString(),
    ) = SolanaAuctionHouseOrderRecord.ExecuteSaleRecord(
        buyer = buyer,
        seller = seller,
        price = price,
        mint = mint,
        treasuryMint = treasuryMint,
        amount = amount,
        direction = direction,
        log = log,
        timestamp = timestamp,
        auctionHouse = auctionHouse,
        orderId = orderId,
    )

    fun randomCancel(
        owner: String = randomAccount(),
        mint: String = randomMint(),
        price: BigInteger = randomBigInt(),
        amount: BigInteger = randomBigInt(),
        direction: OrderDirection = randomDirection(),
        log: SolanaLog = randomSolanaLog(),
        timestamp: Instant = baseTimestamp.plusSeconds(randomLong(86400)),
        auctionHouse: String = randomAccount(),
        orderId: String = randomString(),
    ) = SolanaAuctionHouseOrderRecord.CancelRecord(
        maker = owner,
        mint = mint,
        price = price,
        amount = amount,
        direction = direction,
        log = log,
        timestamp = timestamp,
        auctionHouse = auctionHouse,
        orderId = orderId,
    )

    private fun randomDirection() = OrderDirection.values().random()
    private val baseTimestamp = ZonedDateTime.parse("2022-01-01T00:00:00.000+00:00").toInstant()
}
