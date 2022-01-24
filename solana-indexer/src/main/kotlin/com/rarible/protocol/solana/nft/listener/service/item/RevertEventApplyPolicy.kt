package com.rarible.protocol.solana.nft.listener.service.item

import com.rarible.core.entity.reducer.service.EventApplyPolicy
import com.rarible.protocol.solana.nft.listener.consumer.SolanaLogRecordEvent

open class RevertEventApplyPolicy : EventApplyPolicy<SolanaLogRecordEvent> {
    override fun reduce(
        events: List<SolanaLogRecordEvent>,
        event: SolanaLogRecordEvent
    ): List<SolanaLogRecordEvent> {
        val confirmedEvent = findConfirmedEvent(events, event)

        return if (confirmedEvent != null) events - confirmedEvent else events
    }

    override fun wasApplied(
        events: List<SolanaLogRecordEvent>,
        event: SolanaLogRecordEvent
    ): Boolean {
        return findConfirmedEvent(events, event) != null
    }

    private fun findConfirmedEvent(
        events: List<SolanaLogRecordEvent>,
        event: SolanaLogRecordEvent
    ): SolanaLogRecordEvent? {
        return events.firstOrNull { current ->
            !current.reverted && current.record.log.compareTo(event.record.log) == 0
        }
    }
}
