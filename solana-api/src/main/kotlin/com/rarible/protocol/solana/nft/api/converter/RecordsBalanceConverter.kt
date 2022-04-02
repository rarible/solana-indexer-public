package com.rarible.protocol.solana.nft.api.converter

import com.rarible.protocol.solana.common.records.SolanaBalanceRecord
import com.rarible.protocol.solana.dto.ActivityDto
import com.rarible.protocol.solana.dto.BurnActivityDto
import com.rarible.protocol.solana.dto.MintActivityDto
import com.rarible.protocol.solana.dto.TransferActivityDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull

object RecordsBalanceConverter : ActivityConverter<SolanaBalanceRecord> {
    override fun convert(flow: Flow<SolanaBalanceRecord>): Flow<ActivityDto> =
        flow.mapNotNull(RecordsBalanceConverter::convert)

    private fun convert(record: SolanaBalanceRecord) = when (record) {
        is SolanaBalanceRecord.MintToRecord -> MintActivityDto(
            id = record.id,
            date = record.timestamp,
            owner = record.account,
            tokenAddress = record.mint,
            value = record.mintAmount,
            blockchainInfo = SolanaLogToActivityBlockchainInfoConverter.convert(record.log),
            reverted = false
        )
        is SolanaBalanceRecord.BurnRecord -> BurnActivityDto(
            id = record.id,
            date = record.timestamp,
            owner = record.account,
            tokenAddress = record.mint,
            value = record.burnAmount,
            blockchainInfo = SolanaLogToActivityBlockchainInfoConverter.convert(record.log),
            reverted = false
        )
        is SolanaBalanceRecord.TransferIncomeRecord -> TransferActivityDto(
            id = record.id,
            date = record.timestamp,
            from = record.from,
            owner = record.owner,
            tokenAddress = record.mint,
            value = record.incomeAmount,
            blockchainInfo = SolanaLogToActivityBlockchainInfoConverter.convert(record.log),
            reverted = false,
            purchase = false // TODO should be evaluated
        )
        is SolanaBalanceRecord.TransferOutcomeRecord -> TransferActivityDto(
            id = record.id,
            date = record.timestamp,
            from = record.owner,
            owner = record.to,
            tokenAddress = record.mint,
            value = record.outcomeAmount,
            blockchainInfo = SolanaLogToActivityBlockchainInfoConverter.convert(record.log),
            reverted = false,
            purchase = false // TODO should be evaluated
        )
        is SolanaBalanceRecord.InitializeBalanceAccountRecord -> null
    }
}
