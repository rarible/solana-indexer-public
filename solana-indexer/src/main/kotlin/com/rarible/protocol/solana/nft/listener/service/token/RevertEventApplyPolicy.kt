package com.rarible.protocol.solana.nft.listener.service.token

import com.rarible.core.entity.reducer.service.EventApplyPolicy
import com.rarible.protocol.solana.common.model.EntityEvent

open class RevertEventApplyPolicy<T : EntityEvent> : EventApplyPolicy<T> {
    override fun reduce(
        events: List<T>,
        event: T
    ): List<T> {
        val confirmedEvent = findConfirmedEvent(events, event)

        return if (confirmedEvent != null) events - confirmedEvent else events
    }

    override fun wasApplied(
        events: List<T>,
        event: T
    ): Boolean {
        return findConfirmedEvent(events, event) != null
    }

    private fun findConfirmedEvent(
        events: List<T>,
        event: T
    ): T? {
        return events.firstOrNull { current ->
            !current.reversed && current.log.compareTo(event.log) == 0
        }
    }
}
