package com.rarible.protocol.solana.nft.listener.service.balance

import com.rarible.protocol.solana.nft.listener.consumer.SolanaLogRecordEvent
import com.rarible.protocol.solana.nft.listener.service.records.SolanaItemLogRecord.BurnRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaItemLogRecord.CreateMetadataRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaItemLogRecord.InitializeAccountRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaItemLogRecord.InitializeMintRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaItemLogRecord.MintToRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaItemLogRecord.TransferRecord
import org.springframework.stereotype.Component

@Component
class BalanceEventConverter {
    suspend fun convert(source: SolanaLogRecordEvent): List<BalanceEvent> {
        return when (val record = source.record) {
            is MintToRecord -> listOf(
                BalanceIncomeEvent(
                    reversed = source.reversed,
                    account = record.account,
                    amount = record.mintAmount,
                    log = source.log
                )
            )
            is BurnRecord -> listOf(
                BalanceOutcomeEvent(
                    reversed = source.reversed,
                    account = record.account,
                    amount = record.burnAmount,
                    log = source.log
                )
            )
            is TransferRecord -> listOf(
                BalanceOutcomeEvent(
                    reversed = source.reversed,
                    account = record.from,
                    amount = record.amount,
                    log = source.log
                ),
                BalanceIncomeEvent(
                    reversed = source.reversed,
                    account = record.to,
                    amount = record.amount,
                    log = source.log
                )
            )
            is CreateMetadataRecord, is InitializeAccountRecord, is InitializeMintRecord -> emptyList()
        }
    }
}