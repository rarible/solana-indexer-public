package com.rarible.protocol.solana.test

import com.rarible.blockchain.scanner.solana.model.SolanaLog
import com.rarible.core.common.nowMillis
import com.rarible.core.test.data.randomBoolean
import com.rarible.core.test.data.randomString
import com.rarible.protocol.solana.common.records.SolanaMetaRecord
import java.time.Instant

object MetaplexMetaRecordDataFactory {
    fun randomCreateMetadataAccountRecord(
        mint: String = randomMint(),
        log: SolanaLog = randomSolanaLog(),
        timestamp: Instant = nowMillis()
    ): SolanaMetaRecord.MetaplexCreateMetadataAccountRecord =
        SolanaMetaRecord.MetaplexCreateMetadataAccountRecord(
            meta = createRandomMetaplexMetaFields(),
            metaAccount = randomString(),
            mutable = randomBoolean(),
            mint = mint,
            log = log,
            timestamp = timestamp
        )

}