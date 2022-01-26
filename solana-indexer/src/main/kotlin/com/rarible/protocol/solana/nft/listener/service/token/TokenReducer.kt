package com.rarible.protocol.solana.nft.listener.service.token

import com.rarible.core.entity.reducer.chain.combineIntoChain
import com.rarible.core.entity.reducer.service.Reducer
import com.rarible.protocol.solana.nft.listener.consumer.SolanaLogRecordEvent
import com.rarible.protocol.solana.nft.listener.model.Token
import com.rarible.protocol.solana.nft.listener.service.records.SolanaItemLogRecord.BurnRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaItemLogRecord.MintToRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaItemLogRecord.TransferRecord
import org.springframework.stereotype.Component

@Component
class TokenReducer(
    eventStatusTokenReducer: EventStatusTokenReducer,
    tokenMetricReducer: TokenMetricReducer
) : Reducer<SolanaLogRecordEvent, Token> {

    private val eventStatusItemReducer = combineIntoChain(
        LoggingReducer(),
        tokenMetricReducer,
        eventStatusTokenReducer
    )

    override suspend fun reduce(entity: Token, event: SolanaLogRecordEvent): Token {
        return when (event.record) {
            is BurnRecord, is MintToRecord, is TransferRecord -> eventStatusItemReducer.reduce(entity, event)
            else -> entity
        }
    }
}
