package com.rarible.protocol.solana.nft.listener.consumer

import com.rarible.blockchain.scanner.framework.data.LogRecordEvent
import com.rarible.blockchain.scanner.publisher.KafkaLogRecordEventWrapper
import com.rarible.protocol.solana.common.records.SolanaBaseLogRecord
import org.springframework.stereotype.Component

/**
 * Implementation of the [KafkaLogRecordEventWrapper] that enriches the [LogRecordEvent]
 * with Solana specific JSON subtype info.
 */
@Component
class SolanaKafkaLogRecordEventWrapper : KafkaLogRecordEventWrapper<SolanaLogRecordEvent> {
    override val targetClass: Class<SolanaLogRecordEvent>
        get() = SolanaLogRecordEvent::class.java

    override fun wrap(logRecordEvent: LogRecordEvent): SolanaLogRecordEvent = SolanaLogRecordEvent(
        record = logRecordEvent.record as SolanaBaseLogRecord,
        reversed = logRecordEvent.reverted
    )
}