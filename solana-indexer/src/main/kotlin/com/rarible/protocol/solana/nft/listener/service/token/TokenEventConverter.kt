package com.rarible.protocol.solana.nft.listener.service.token

import com.rarible.protocol.solana.common.event.BurnEvent
import com.rarible.protocol.solana.common.event.MintEvent
import com.rarible.protocol.solana.common.event.TokenEvent
import com.rarible.protocol.solana.common.event.TransferEvent
import com.rarible.protocol.solana.nft.listener.consumer.SolanaLogRecordEvent
import com.rarible.protocol.solana.nft.listener.service.records.SolanaItemLogRecord.BurnRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaItemLogRecord.CreateMetadataRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaItemLogRecord.InitializeAccountRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaItemLogRecord.InitializeMintRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaItemLogRecord.MintToRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaItemLogRecord.TransferRecord
import org.springframework.stereotype.Component

@Component
class TokenEventConverter {
    suspend fun convert(source: SolanaLogRecordEvent): List<TokenEvent> {
        return when (val record = source.record) {
            is MintToRecord -> listOf(
                MintEvent(
                    token = record.mint,
                    reversed = source.reversed,
                    amount = record.mintAmount,
                    log = source.record.log
                )
            )
            is BurnRecord -> listOf(
                BurnEvent(
                    reversed = source.reversed,
                    token = record.mint,
                    amount = record.burnAmount,
                    log = source.record.log
                )
            )
            is TransferRecord -> listOf(
                TransferEvent(
                    reversed = source.reversed,
                    token = record.mint,
                    from = record.from,
                    to = record.to,
                    amount = record.amount,
                    log = source.record.log
                )
            )
            is CreateMetadataRecord, is InitializeAccountRecord, is InitializeMintRecord -> emptyList()
        }
    }
}