package com.rarible.protocol.solana.nft.listener.service.item

import com.rarible.core.entity.reducer.service.Reducer
import com.rarible.protocol.solana.nft.listener.consumer.SolanaLogRecordEvent
import com.rarible.protocol.solana.nft.listener.model.Item
import org.springframework.stereotype.Component

@Component
class EventStatusItemReducer(
    private val forwardChainItemReducer: ForwardChainItemReducer,
    private val reversedChainItemReducer: ReversedChainItemReducer,
) : Reducer<SolanaLogRecordEvent, Item> {
    override suspend fun reduce(entity: Item, event: SolanaLogRecordEvent): Item {
        return if (event.reverted) {
            reversedChainItemReducer.reduce(entity, event)
        } else {
            forwardChainItemReducer.reduce(entity, event)
        }
    }
}