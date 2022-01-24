package com.rarible.protocol.solana.nft.listener.service.item

import com.rarible.core.entity.reducer.service.Reducer
import com.rarible.protocol.solana.nft.listener.consumer.SolanaLogRecordEvent
import com.rarible.protocol.solana.nft.listener.model.Item
import com.rarible.protocol.solana.nft.listener.service.records.SolanaItemLogRecord.BurnRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaItemLogRecord.CreateMetadataRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaItemLogRecord.InitializeAccountRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaItemLogRecord.InitializeMintRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaItemLogRecord.MintToRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaItemLogRecord.TransferRecord
import org.springframework.stereotype.Component

@Component
class ReversedValueItemReducer : Reducer<SolanaLogRecordEvent, Item> {
    private val forwardValueItemReducer = ForwardValueItemReducer()

    override suspend fun reduce(entity: Item, event: SolanaLogRecordEvent): Item {
        return when (event.record) {
            is BurnRecord,
            is TransferRecord,
            is MintToRecord,
            is CreateMetadataRecord -> forwardValueItemReducer.reduce(entity, event)
            is InitializeAccountRecord, is InitializeMintRecord -> TODO()
        }
    }
}

