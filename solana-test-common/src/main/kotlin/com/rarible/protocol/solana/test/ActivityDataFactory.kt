package com.rarible.protocol.solana.test

import com.rarible.blockchain.scanner.solana.model.SolanaLog
import com.rarible.core.test.data.randomBigInt
import com.rarible.core.test.data.randomLong
import com.rarible.core.test.data.randomString
import com.rarible.protocol.solana.common.records.OrderDirection
import com.rarible.protocol.solana.common.records.SolanaAuctionHouseOrderRecord
import com.rarible.protocol.solana.common.records.SolanaBalanceRecord
import java.math.BigInteger
import java.time.Instant
import java.time.ZonedDateTime

object ActivityDataFactory {

    fun List<SolanaAuctionHouseOrderRecord>.turnLog(log: SolanaLog) = map { record ->
        when (record) {
            is SolanaAuctionHouseOrderRecord.BuyRecord -> record.copy(log = log)
            is SolanaAuctionHouseOrderRecord.CancelRecord -> record.copy(log = log)
            is SolanaAuctionHouseOrderRecord.ExecuteSaleRecord -> record.copy(log = log)
            is SolanaAuctionHouseOrderRecord.SellRecord -> record.copy(log = log)
        }
    }

    fun randomMintToRecord(
        mintAmount: BigInteger = randomBigInt(),
        mint: String = randomString(),
        account: String = randomString(),
        log: SolanaLog = randomSolanaLog(),
        timestamp: Instant = baseTimestamp.plusSeconds(randomLong(86400)),
    ) = SolanaBalanceRecord.MintToRecord(
        mintAmount = mintAmount,
        mint = mint,
        account = account,
        log = log,
        timestamp = timestamp
    )

    fun randomBurnRecord(
        burnAmount: BigInteger = randomBigInt(),
        mint: String = randomString(),
        account: String = randomString(),
        log: SolanaLog = randomSolanaLog(),
        timestamp: Instant = baseTimestamp.plusSeconds(randomLong(86400)),
    ) = SolanaBalanceRecord.BurnRecord(
        burnAmount = burnAmount,
        mint = mint,
        account = account,
        log = log,
        timestamp = timestamp
    )

    fun randomIncomeRecord(
        from: String = randomString(),
        owner: String = randomString(),
        mint: String = randomString(),
        incomeAmount: BigInteger = randomBigInt(),
        log: SolanaLog = randomSolanaLog(),
        timestamp: Instant = baseTimestamp.plusSeconds(randomLong(86400)),
    ) = SolanaBalanceRecord.TransferIncomeRecord(
        from = from,
        owner = owner,
        mint = mint,
        incomeAmount = incomeAmount,
        log = log,
        timestamp = timestamp
    )

    fun randomOutcomeRecord(
        to: String = randomString(),
        owner: String = randomString(),
        mint: String = randomString(),
        outcomeAmount: BigInteger = randomBigInt(),
        log: SolanaLog = randomSolanaLog(),
        timestamp: Instant = baseTimestamp.plusSeconds(randomLong(86400)),
    ) = SolanaBalanceRecord.TransferOutcomeRecord(
        to = to,
        owner = owner,
        mint = mint,
        outcomeAmount = outcomeAmount,
        log = log,
        timestamp = timestamp
    )

    fun randomBuyRecord(
        maker: String = randomString(),
        treasuryMint: String = randomString(),
        buyPrice: BigInteger = randomBigInt(),
        tokenAccount: String = randomString(),
        mint: String = randomString(),
        amount: BigInteger = randomBigInt(),
        log: SolanaLog = randomSolanaLog(),
        timestamp: Instant = baseTimestamp.plusSeconds(randomLong(86400)),
        auctionHouse: String = randomString(),
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
        maker: String = randomString(),
        sellPrice: BigInteger = randomBigInt(),
        tokenAccount: String = randomString(),
        mint: String = randomString(),
        amount: BigInteger = randomBigInt(),
        log: SolanaLog = randomSolanaLog(),
        timestamp: Instant = baseTimestamp.plusSeconds(randomLong(86400)),
        auctionHouse: String = randomString(),
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
        buyer: String = randomString(),
        seller: String = randomString(),
        price: BigInteger = randomBigInt(),
        mint: String = randomString(),
        treasuryMint: String = randomString(),
        amount: BigInteger = randomBigInt(),
        direction: OrderDirection = randomDirection(),
        log: SolanaLog = randomSolanaLog(),
        timestamp: Instant = baseTimestamp.plusSeconds(randomLong(86400)),
        auctionHouse: String = randomString(),
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
        owner: String = randomString(),
        mint: String = randomString(),
        price: BigInteger = randomBigInt(),
        amount: BigInteger = randomBigInt(),
        direction: OrderDirection = randomDirection(),
        log: SolanaLog = randomSolanaLog(),
        timestamp: Instant = baseTimestamp.plusSeconds(randomLong(86400)),
        auctionHouse: String = randomString(),
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
