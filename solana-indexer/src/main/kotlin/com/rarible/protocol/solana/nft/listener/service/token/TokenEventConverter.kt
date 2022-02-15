package com.rarible.protocol.solana.nft.listener.service.token

import com.rarible.protocol.solana.common.event.BurnEvent
import com.rarible.protocol.solana.common.event.InitializeMintEvent
import com.rarible.protocol.solana.common.event.MintEvent
import com.rarible.protocol.solana.common.event.TokenEvent
import com.rarible.protocol.solana.nft.listener.consumer.SolanaLogRecordEvent
import com.rarible.protocol.solana.nft.listener.service.records.SolanaBaseLogRecord.BurnRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaBaseLogRecord.InitializeMintRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaBaseLogRecord.MintToRecord
import org.springframework.stereotype.Component

@Component
class TokenEventConverter {
    suspend fun convert(source: SolanaLogRecordEvent): List<TokenEvent> {
        val timestamp = source.record.timestamp
        return when (val record = source.record) {
            is MintToRecord -> listOf(
                MintEvent(
                    token = record.mint,
                    reversed = source.reversed,
                    amount = record.mintAmount,
                    log = source.record.log,
                    timestamp = timestamp
                )
            )
            is BurnRecord -> listOf(
                BurnEvent(
                    reversed = source.reversed,
                    token = record.mint,
                    amount = record.burnAmount,
                    log = source.record.log,
                    timestamp = timestamp
                )
            )
            is InitializeMintRecord -> listOf(
                InitializeMintEvent(
                    log = source.record.log,
                    reversed = source.reversed,
                    token = record.mint,
                    timestamp = timestamp
                )
            )
            else -> emptyList()
        }
    }
}
