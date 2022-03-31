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

    fun randomMintToRecord(
        mintAmount: BigInteger? = null,
        mint: String? = null,
        account: String? = null,
        log: SolanaLog? = null,
        timestamp: Instant? = null,
    ) = SolanaBalanceRecord.MintToRecord(
        mintAmount = mintAmount ?: randomBigInt(),
        mint = mint ?: randomString(),
        account = account ?: randomString(),
        log = log ?: randomSolanaLog(),
        timestamp = timestamp ?: baseTimestamp.plusSeconds(randomLong(86400))
    )

    fun randomBurnRecord(
        burnAmount: BigInteger? = null,
        mint: String? = null,
        account: String? = null,
        log: SolanaLog? = null,
        timestamp: Instant? = null,
    ) = SolanaBalanceRecord.BurnRecord(
        burnAmount = burnAmount ?: randomBigInt(),
        mint = mint ?: randomString(),
        account = account ?: randomString(),
        log = log ?: randomSolanaLog(),
        timestamp = timestamp ?: baseTimestamp.plusSeconds(randomLong(86400))
    )

    fun randomIncomeRecord(
        from: String? = null,
        owner: String? = null,
        mint: String? = null,
        incomeAmount: BigInteger? = null,
        log: SolanaLog? = null,
        timestamp: Instant? = null,
    ) = SolanaBalanceRecord.TransferIncomeRecord(
        from = from ?: randomString(),
        owner = owner ?: randomString(),
        mint = mint ?: randomString(),
        incomeAmount = incomeAmount ?: randomBigInt(),
        log = log ?: randomSolanaLog(),
        timestamp = timestamp ?: baseTimestamp.plusSeconds(randomLong(86400))
    )

    fun randomOutcomeRecord(
        to: String? = null,
        owner: String? = null,
        mint: String? = null,
        outcomeAmount: BigInteger? = null,
        log: SolanaLog? = null,
        timestamp: Instant? = null,
    ) = SolanaBalanceRecord.TransferOutcomeRecord(
        to = to ?: randomString(),
        owner = owner ?: randomString(),
        mint = mint ?: randomString(),
        outcomeAmount = outcomeAmount ?: randomBigInt(),
        log = log ?: randomSolanaLog(),
        timestamp = timestamp ?: baseTimestamp.plusSeconds(randomLong(86400))
    )

    fun randomBuyRecord(
        maker: String? = null,
        treasuryMint: String? = null,
        buyPrice: BigInteger? = null,
        tokenAccount: String? = null,
        mint: String? = null,
        amount: BigInteger? = null,
        log: SolanaLog? = null,
        timestamp: Instant? = null,
        auctionHouse: String? = null,
        orderId: String? = null,
    ) = SolanaAuctionHouseOrderRecord.BuyRecord(
        maker = maker ?: randomString(),
        treasuryMint = treasuryMint ?: randomString(),
        buyPrice = buyPrice ?: randomBigInt(),
        tokenAccount = tokenAccount ?: randomString(),
        mint = mint ?: randomString(),
        amount = amount ?: randomBigInt(),
        log = log ?: randomSolanaLog(),
        timestamp = timestamp ?: baseTimestamp.plusSeconds(randomLong(86400)),
        auctionHouse = auctionHouse ?: randomString(),
        orderId = orderId ?: randomString(),
    )

    fun randomSellRecord(
        maker: String? = null,
        sellPrice: BigInteger? = null,
        tokenAccount: String? = null,
        mint: String? = null,
        amount: BigInteger? = null,
        log: SolanaLog? = null,
        timestamp: Instant? = null,
        auctionHouse: String? = null,
        orderId: String? = null,
    ) = SolanaAuctionHouseOrderRecord.SellRecord(
        maker = maker ?: randomString(),
        sellPrice = sellPrice ?: randomBigInt(),
        tokenAccount = tokenAccount ?: randomString(),
        mint = mint ?: randomString(),
        amount = amount ?: randomBigInt(),
        log = log ?: randomSolanaLog(),
        timestamp = timestamp ?: baseTimestamp.plusSeconds(randomLong(86400)),
        auctionHouse = auctionHouse ?: randomString(),
        orderId = orderId ?: randomString(),
    )

    fun randomExecuteSaleRecord(
        buyer: String? = null,
        seller: String? = null,
        price: BigInteger? = null,
        mint: String? = null,
        treasuryMint: String? = null,
        amount: BigInteger? = null,
        direction: OrderDirection? = null,
        log: SolanaLog? = null,
        timestamp: Instant? = null,
        auctionHouse: String? = null,
        orderId: String? = null,
    ) = SolanaAuctionHouseOrderRecord.ExecuteSaleRecord(
        buyer = buyer ?: randomString(),
        seller = seller ?: randomString(),
        price = price ?: randomBigInt(),
        mint = mint ?: randomString(),
        treasuryMint = treasuryMint ?: randomString(),
        amount = amount ?: randomBigInt(),
        direction = direction ?: randomDirection(),
        log = log ?: randomSolanaLog(),
        timestamp = timestamp ?: baseTimestamp.plusSeconds(randomLong(86400)),
        auctionHouse = auctionHouse ?: randomString(),
        orderId = orderId ?: randomString(),
    )

    fun randomCancel(
        owner: String? = null,
        mint: String? = null,
        price: BigInteger? = null,
        amount: BigInteger? = null,
        log: SolanaLog? = null,
        timestamp: Instant? = null,
        auctionHouse: String? = null,
        orderId: String? = null,
    ) = SolanaAuctionHouseOrderRecord.CancelRecord(
        maker = owner ?: randomString(),
        mint = mint ?: randomString(),
        price = price ?: randomBigInt(),
        amount = amount ?: randomBigInt(),
        direction = randomDirection(),
        log = log ?: randomSolanaLog(),
        timestamp = timestamp ?: baseTimestamp.plusSeconds(randomLong(86400)),
        auctionHouse = auctionHouse ?: randomString(),
        orderId = orderId ?: randomString(),
    )

    private fun randomDirection() = OrderDirection.values().random()
    private val baseTimestamp = ZonedDateTime.parse("2022-01-01T00:00:00.000+00:00").toInstant()
}
