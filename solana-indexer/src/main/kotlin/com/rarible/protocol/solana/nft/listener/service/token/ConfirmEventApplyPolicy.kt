package com.rarible.protocol.solana.nft.listener.service.token

import com.rarible.core.entity.reducer.service.EventApplyPolicy
import com.rarible.protocol.solana.common.model.EntityEvent

open class ConfirmEventApplyPolicy<T : EntityEvent>(
    private val confirmationBlocks: Int
) : EventApplyPolicy<T> {

    override fun reduce(events: List<T>, event: T): List<T> {
        val newEventList = (events + event)
        val lastNotRevertableEvent = newEventList.lastOrNull { current ->
            !current.reversed && isNotReverted(incomeEvent = event, current = current)
        }
        return newEventList.filter { current ->
            current.reversed || current == lastNotRevertableEvent || isReverted(
                incomeEvent = event,
                current = current
            )
        }
    }

    override fun wasApplied(events: List<T>, event: T): Boolean {
        val lastAppliedEvent = events.lastOrNull { !it.reversed }

        return !(lastAppliedEvent == null || lastAppliedEvent.log < event.log)
    }

    private fun isReverted(incomeEvent: T, current: T): Boolean {
        return isNotReverted(incomeEvent, current).not()
    }

    private fun isNotReverted(incomeEvent: T, current: T): Boolean {
        val incomeBlockNumber = requireNotNull(incomeEvent.log.blockNumber)
        val currentBlockNumber = requireNotNull(current.log.blockNumber)
        val blockDiff = incomeBlockNumber - currentBlockNumber

        require(blockDiff >= 0) {
            "Block diff between income=$incomeEvent and current=$current can't be negative"
        }
        return blockDiff >= confirmationBlocks
    }
}
