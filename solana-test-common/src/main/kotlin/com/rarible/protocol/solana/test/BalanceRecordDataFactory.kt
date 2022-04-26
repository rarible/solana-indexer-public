package com.rarible.protocol.solana.test

import com.rarible.blockchain.scanner.solana.model.SolanaLog
import com.rarible.core.common.nowMillis
import com.rarible.core.test.data.randomBigInt
import com.rarible.core.test.data.randomLong
import com.rarible.protocol.solana.common.records.SolanaBalanceRecord
import java.math.BigInteger
import java.time.Instant
import java.time.ZonedDateTime

object BalanceRecordDataFactory {

    fun randomBalanceInitRecord(
        account: String = randomAccount(),
        owner: String = randomAccount(),
        mint: String = randomMint(),
        log: SolanaLog = randomSolanaLog(),
        timestamp: Instant = nowMillis()
    ): SolanaBalanceRecord.InitializeBalanceAccountRecord =
        SolanaBalanceRecord.InitializeBalanceAccountRecord(
            account = account,
            owner = owner,
            mint = mint,
            log = log,
            timestamp = timestamp
        )

    fun randomMintToRecord(
        mintAmount: BigInteger = randomBigInt(),
        mint: String = randomMint(),
        account: String = randomAccount(),
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
        mint: String = randomMint(),
        account: String = randomAccount(),
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
        from: String = randomAccount(),
        account: String = randomAccount(),
        mint: String = randomMint(),
        incomeAmount: BigInteger = randomBigInt(),
        log: SolanaLog = randomSolanaLog(),
        timestamp: Instant = baseTimestamp.plusSeconds(randomLong(86400)),
    ) = SolanaBalanceRecord.TransferIncomeRecord(
        from = from,
        account = account,
        mint = mint,
        incomeAmount = incomeAmount,
        log = log,
        timestamp = timestamp
    )

    fun randomOutcomeRecord(
        to: String = randomAccount(),
        account: String = randomAccount(),
        mint: String = randomMint(),
        outcomeAmount: BigInteger = randomBigInt(),
        log: SolanaLog = randomSolanaLog(),
        timestamp: Instant = baseTimestamp.plusSeconds(randomLong(86400)),
    ) = SolanaBalanceRecord.TransferOutcomeRecord(
        to = to,
        account = account,
        mint = mint,
        outcomeAmount = outcomeAmount,
        log = log,
        timestamp = timestamp
    )

    private val baseTimestamp = ZonedDateTime.parse("2022-01-01T00:00:00.000+00:00").toInstant()
}
