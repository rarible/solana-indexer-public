package com.rarible.protocol.solana.common.records

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.rarible.blockchain.scanner.solana.model.SolanaLogRecord
import java.time.Instant

// Caution! All subclasses must have a unique set of field names to make the JSON deduction work as expected.
@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
sealed class SolanaBaseLogRecord : SolanaLogRecord() {
    abstract val timestamp: Instant
}
