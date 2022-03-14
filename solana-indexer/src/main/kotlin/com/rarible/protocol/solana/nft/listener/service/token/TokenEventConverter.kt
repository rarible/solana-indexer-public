package com.rarible.protocol.solana.nft.listener.service.token

import com.rarible.protocol.solana.common.event.BurnEvent
import com.rarible.protocol.solana.common.event.InitializeMintEvent
import com.rarible.protocol.solana.common.event.MintEvent
import com.rarible.protocol.solana.common.event.TokenEvent
import com.rarible.protocol.solana.nft.listener.service.records.SolanaTokenRecord
import org.springframework.stereotype.Component

@Component
class TokenEventConverter {
    suspend fun convert(
        record: SolanaTokenRecord,
        reversed: Boolean
    ): List<TokenEvent> = when (record) {
        is SolanaTokenRecord.MintToRecord -> listOf(
            MintEvent(
                token = record.mint,
                reversed = reversed,
                amount = record.mintAmount,
                log = record.log,
                timestamp = record.timestamp
            )
        )
        is SolanaTokenRecord.BurnRecord -> listOf(
            BurnEvent(
                reversed = reversed,
                token = record.mint,
                amount = record.burnAmount,
                log = record.log,
                timestamp = record.timestamp
            )
        )
        is SolanaTokenRecord.InitializeMintRecord -> listOf(
            InitializeMintEvent(
                log = record.log,
                reversed = reversed,
                token = record.mint,
                timestamp = record.timestamp
            )
        )
    }
}
