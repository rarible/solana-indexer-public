package com.rarible.protocol.solana.nft.listener.service.balance

import com.rarible.protocol.solana.common.event.BalanceEvent
import com.rarible.protocol.solana.common.event.BalanceIncomeEvent
import com.rarible.protocol.solana.common.event.BalanceOutcomeEvent
import com.rarible.protocol.solana.nft.listener.consumer.SolanaLogRecordEvent
import com.rarible.protocol.solana.nft.listener.service.records.SolanaBalanceRecord
import org.springframework.stereotype.Component

@Component
class BalanceEventConverter {
    suspend fun convert(source: SolanaLogRecordEvent): List<BalanceEvent> {
        return when (val record = source.record) {
            is SolanaBalanceRecord.MintToRecord -> listOf(
                BalanceIncomeEvent(
                    reversed = source.reversed,
                    account = record.account,
                    amount = record.mintAmount,
                    log = source.record.log,
                    timestamp = source.record.timestamp
                )
            )
            is SolanaBalanceRecord.BurnRecord -> listOf(
                BalanceOutcomeEvent(
                    reversed = source.reversed,
                    account = record.account,
                    amount = record.burnAmount,
                    log = source.record.log,
                    timestamp = source.record.timestamp
                )
            )
            is SolanaBalanceRecord.TransferIncomeRecord -> listOf(
                BalanceIncomeEvent(
                    reversed = source.reversed,
                    account = record.to,
                    amount = record.incomeAmount,
                    log = source.record.log,
                    timestamp = source.record.timestamp
                )
            )
            is SolanaBalanceRecord.TransferOutcomeRecord -> listOf(
                BalanceOutcomeEvent(
                    reversed = source.reversed,
                    account = record.from,
                    amount = record.outcomeAmount,
                    log = source.record.log,
                    timestamp = source.record.timestamp
                )
            )
            else -> emptyList()
        }
    }
}
