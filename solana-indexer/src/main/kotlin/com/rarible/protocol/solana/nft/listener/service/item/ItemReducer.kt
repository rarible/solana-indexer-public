package com.rarible.protocol.solana.nft.listener.service.item

import com.rarible.blockchain.scanner.framework.data.LogRecordEvent
import com.rarible.core.entity.reducer.chain.combineIntoChain
import com.rarible.core.entity.reducer.service.Reducer
import com.rarible.protocol.solana.nft.listener.model.Item
import com.rarible.protocol.solana.nft.listener.model.ItemEvent
import com.rarible.protocol.solana.nft.listener.service.records.SolanaItemLogRecord.BurnRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaItemLogRecord.MintToRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaItemLogRecord.TransferRecord
import org.springframework.stereotype.Component

@Component
class ItemReducer(
    eventStatusItemReducer: EventStatusItemReducer,
    itemMetricReducer: ItemMetricReducer
) : Reducer<LogRecordEvent<ItemEvent>, Item> {

    private val eventStatusItemReducer = combineIntoChain(
        LoggingReducer(),
        itemMetricReducer,
        eventStatusItemReducer
    )

    override suspend fun reduce(entity: Item, event: LogRecordEvent<ItemEvent>): Item {
        return when (event.record) {
            is BurnRecord, is MintToRecord, is TransferRecord -> eventStatusItemReducer.reduce(entity, event)
            else -> entity
        }
    }
}
