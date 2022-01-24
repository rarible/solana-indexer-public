package com.rarible.protocol.solana.nft.listener.service.item

import com.rarible.blockchain.scanner.framework.data.LogRecordEvent
import com.rarible.core.entity.reducer.service.Reducer
import com.rarible.protocol.solana.nft.listener.model.Item
import com.rarible.protocol.solana.nft.listener.model.ItemEvent
import org.springframework.stereotype.Component

@Component
class EventStatusItemReducer(
    private val forwardChainItemReducer: ForwardChainItemReducer,
    private val reversedChainItemReducer: ReversedChainItemReducer,
) : Reducer<LogRecordEvent<ItemEvent>, Item> {
    override suspend fun reduce(entity: Item, event: LogRecordEvent<ItemEvent>): Item {
        return if (event.reverted) {
            reversedChainItemReducer.reduce(entity, event)
        } else {
            forwardChainItemReducer.reduce(entity, event)
        }
    }
}