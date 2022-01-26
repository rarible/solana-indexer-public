package com.rarible.protocol.solana.nft.listener.service.token

import com.rarible.core.entity.reducer.service.Reducer
import com.rarible.protocol.solana.nft.listener.consumer.SolanaLogRecordEvent
import com.rarible.protocol.solana.nft.listener.model.Token
import com.rarible.protocol.solana.nft.listener.service.records.SolanaItemLogRecord.BurnRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaItemLogRecord.CreateMetadataRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaItemLogRecord.InitializeAccountRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaItemLogRecord.InitializeMintRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaItemLogRecord.MintToRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaItemLogRecord.TransferRecord
import org.springframework.stereotype.Component

@Component
class ReversedValueTokenReducer : Reducer<SolanaLogRecordEvent, Token> {
    private val forwardValueTokenReducer = ForwardValueTokenReducer()

    override suspend fun reduce(entity: Token, event: SolanaLogRecordEvent): Token {
        return when (event.record) {
            is BurnRecord,
            is TransferRecord,
            is MintToRecord,
            is CreateMetadataRecord -> forwardValueTokenReducer.reduce(entity, event.copy(record = event.record.invert()))
            is InitializeAccountRecord, is InitializeMintRecord -> TODO()
        }
    }
}

