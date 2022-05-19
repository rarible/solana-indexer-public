package com.rarible.protocol.solana.test

import com.rarible.blockchain.scanner.solana.model.SolanaLog
import com.rarible.core.common.nowMillis
import com.rarible.core.test.data.randomInt
import com.rarible.protocol.solana.common.records.SolanaTokenRecord
import java.time.Instant

object TokenRecordDataFactory {
    fun randomTokenInitRecord(
        mintAuthority: String = randomAccount(),
        decimals: Int = randomInt(0, 9),
        mint: String = randomMint(),
        log: SolanaLog = randomSolanaLog(),
        timestamp: Instant = nowMillis()
    ): SolanaTokenRecord.InitializeMintRecord =
        SolanaTokenRecord.InitializeMintRecord(
            mintAuthority = mintAuthority,
            decimals = decimals,
            mint = mint,
            log = log,
            timestamp = timestamp
        )
}