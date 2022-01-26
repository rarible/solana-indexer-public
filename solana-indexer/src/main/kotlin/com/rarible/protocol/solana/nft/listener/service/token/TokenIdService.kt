package com.rarible.protocol.solana.nft.listener.service.token

import com.rarible.core.entity.reducer.service.EntityIdService
import com.rarible.protocol.solana.nft.listener.consumer.SolanaLogRecordEvent
import com.rarible.protocol.solana.nft.listener.model.TokenId
import com.rarible.protocol.solana.nft.listener.service.records.SolanaItemLogRecord.BurnRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaItemLogRecord.CreateMetadataRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaItemLogRecord.InitializeAccountRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaItemLogRecord.InitializeMintRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaItemLogRecord.MintToRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaItemLogRecord.TransferRecord
import org.springframework.stereotype.Component

@Component
class TokenIdService : EntityIdService<SolanaLogRecordEvent, TokenId> {
    override fun getEntityId(event : SolanaLogRecordEvent): TokenId {
        // todo mb exract abstract mint field?
        return when (val record = event.record) {
            is BurnRecord -> record.mint
            is CreateMetadataRecord -> record.mint
            is InitializeAccountRecord -> record.mint
            is InitializeMintRecord -> record.mint
            is MintToRecord -> record.mint
            is TransferRecord -> record.mint
        }
    }
}
