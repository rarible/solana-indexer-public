package com.rarible.protocol.solana.nft.listener.service.item

import com.rarible.core.entity.reducer.service.EventApplyPolicy
import com.rarible.protocol.solana.nft.listener.consumer.SolanaLogRecordEvent

open class ConfirmEventApplyPolicy(
    private val confirmationBlocks: Int
) : EventApplyPolicy<SolanaLogRecordEvent> {

    override fun reduce(events: List<SolanaLogRecordEvent>, event: SolanaLogRecordEvent): List<SolanaLogRecordEvent> {
        val newEventList = (events + event)
        val lastNotRevertableEvent = newEventList.lastOrNull { current ->
            !current.reverted && isNotReverted(incomeEvent = event, current = current)
        }
        return newEventList.filter { current ->
            current.reverted || current == lastNotRevertableEvent || isReverted(
                incomeEvent = event,
                current = current
            )
        }
    }

    override fun wasApplied(events: List<SolanaLogRecordEvent>, event: SolanaLogRecordEvent): Boolean {
        val lastAppliedEvent = events.lastOrNull { !it.reverted }

        return !(lastAppliedEvent == null || lastAppliedEvent.record.log < event.record.log)
    }

    private fun isReverted(incomeEvent: SolanaLogRecordEvent, current: SolanaLogRecordEvent): Boolean {
        return isNotReverted(incomeEvent, current).not()
    }

    private fun isNotReverted(incomeEvent: SolanaLogRecordEvent, current: SolanaLogRecordEvent): Boolean {
        val incomeBlockNumber = requireNotNull(incomeEvent.record.log.blockNumber)
        val currentBlockNumber = requireNotNull(current.record.log.blockNumber)
        val blockDiff = incomeBlockNumber - currentBlockNumber

        require(blockDiff >= 0) {
            "Block diff between income=$incomeEvent and current=$current can't be negative"
        }
        return blockDiff >= confirmationBlocks
    }
}
