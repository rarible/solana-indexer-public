package com.rarible.protocol.solana.nft.listener.service.item

import com.rarible.core.entity.reducer.chain.combineIntoChain
import com.rarible.core.entity.reducer.service.Reducer
import com.rarible.protocol.solana.nft.listener.consumer.SolanaLogRecordEvent
import com.rarible.protocol.solana.nft.listener.model.Item
import com.rarible.protocol.solana.nft.listener.service.records.SolanaItemLogRecord.BurnRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaItemLogRecord.MintToRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaItemLogRecord.TransferRecord
import org.springframework.stereotype.Component

@Component
class ItemReducer(
    eventStatusItemReducer: EventStatusItemReducer,
    itemMetricReducer: ItemMetricReducer
) : Reducer<SolanaLogRecordEvent, Item> {

    private val eventStatusItemReducer = combineIntoChain(
        LoggingReducer(),
        itemMetricReducer,
        eventStatusItemReducer
    )

    override suspend fun reduce(entity: Item, event: SolanaLogRecordEvent): Item {
        return when (event.record) {
            is BurnRecord, is MintToRecord, is TransferRecord -> eventStatusItemReducer.reduce(entity, event)
            else -> entity
        }
    }
}
