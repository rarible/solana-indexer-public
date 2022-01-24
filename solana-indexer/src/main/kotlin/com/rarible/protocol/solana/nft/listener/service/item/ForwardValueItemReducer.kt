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
class ForwardValueItemReducer : Reducer<SolanaLogRecordEvent, Item> {
    override suspend fun reduce(entity: Item, event: SolanaLogRecordEvent): Item {
        return when (val record = event.record) {
            is MintToRecord -> entity.copy(supply = entity.supply + record.mintAmount)
            is BurnRecord -> entity.copy(supply = entity.supply - record.burnAmount)
            is CreateMetadataRecord, is InitializeAccountRecord, is InitializeMintRecord, is TransferRecord -> entity
        }
    }
}