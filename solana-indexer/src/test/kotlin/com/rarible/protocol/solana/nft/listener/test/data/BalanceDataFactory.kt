package com.rarible.protocol.solana.nft.listener.test.data

import com.rarible.core.common.nowMillis
import com.rarible.core.test.data.randomBigInt
import com.rarible.core.test.data.randomString
import com.rarible.protocol.solana.common.records.SolanaBalanceRecord
import com.rarible.protocol.solana.test.randomSolanaLog

fun randomBalanceInitRecord(): SolanaBalanceRecord.InitializeBalanceAccountRecord {
    return SolanaBalanceRecord.InitializeBalanceAccountRecord(
        balanceAccount = randomString(),
        owner = randomString(),
        mint = randomString(),
        log = randomSolanaLog(),
        timestamp = nowMillis()
    )
}

fun randomBalanceMintRecord(): SolanaBalanceRecord.MintToRecord {
    return SolanaBalanceRecord.MintToRecord(
        mint = randomString(),
        log = randomSolanaLog(),
        timestamp = nowMillis(),
        mintAmount = randomBigInt(),
        account = randomString()
    )
}

fun randomBalanceBurnRecord(): SolanaBalanceRecord.BurnRecord {
    return SolanaBalanceRecord.BurnRecord(
        mint = randomString(),
        log = randomSolanaLog(),
        timestamp = nowMillis(),
        burnAmount = randomBigInt(),
        account = randomString()
    )
}

fun randomBalanceIncomeTransfer(): SolanaBalanceRecord.TransferIncomeRecord {
    return SolanaBalanceRecord.TransferIncomeRecord(
        mint = null,
        log = randomSolanaLog(),
        timestamp = nowMillis(),
        from = randomString(),
        to = randomString(),
        incomeAmount = randomBigInt()
    )
}

fun randomBalanceOutcomeTransfer(): SolanaBalanceRecord.TransferOutcomeRecord {
    return SolanaBalanceRecord.TransferOutcomeRecord(
        mint = null,
        log = randomSolanaLog(),
        timestamp = nowMillis(),
        from = randomString(),
        to = randomString(),
        outcomeAmount = randomBigInt()
    )
}
