package com.rarible.protocol.solana.nft.api.service

import com.rarible.blockchain.scanner.solana.model.SolanaLog
import com.rarible.protocol.solana.common.records.SolanaBalanceRecord
import com.rarible.protocol.solana.common.repository.RecordsBalanceRepository
import com.rarible.solana.protocol.dto.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Component

@Component
class ActivityApiService(
    private val recordsBalanceRepository: RecordsBalanceRepository,
) {
    suspend fun getActivitiesByItem(
        type: List<ActivityTypeDto>,
        tokenAddress: String,
        continuation: String?,
        size: Int?,
        sort: ActivitySortDto,
    ) = getActivities(size) { actualSize ->
        recordsBalanceRepository.findByItem(type, tokenAddress, continuation, actualSize, sort)
    }

    suspend fun getActivitiesByCollection(
        type: List<ActivityTypeDto>,
        collection: String,
        continuation: String?,
        size: Int?,
        sort: ActivitySortDto,
    ) = getActivities(size) { actualSize ->
        recordsBalanceRepository.findByCollection(type, collection, continuation, actualSize, sort)
    }

    suspend fun getAllActivities(
        type: List<ActivityTypeDto>,
        continuation: String?,
        size: Int?,
        sort: ActivitySortDto,
    ) = getActivities(size) { actualSize ->
        recordsBalanceRepository.findAll(type, continuation, actualSize, sort)
    }

    private suspend fun getActivities(size: Int?, block: (Int) -> Flow<SolanaBalanceRecord>): ActivitiesDto {
        val actualSize = size ?: DEFAULT_SIZE

        val activities = block(actualSize).mapNotNull { convert(it) }.toList()

        val outContinuation = activities
            .takeIf { activities.size >= actualSize }
            .let { activities.lastOrNull()?.let(this::makeContinuation) }

        return ActivitiesDto(outContinuation, activities)
    }

    private fun makeContinuation(last: ActivityDto) = "${last.date.toEpochMilli()}_${last.id}"

    private fun convert(it: SolanaBalanceRecord) = when (it) {
        is SolanaBalanceRecord.MintToRecord -> makeMint(it)
        is SolanaBalanceRecord.BurnRecord -> makeBurn(it)
        is SolanaBalanceRecord.TransferIncomeRecord -> makeTransferIn(it)
        is SolanaBalanceRecord.TransferOutcomeRecord -> makeTransferOut(it)
        is SolanaBalanceRecord.InitializeBalanceAccountRecord -> null
    }

    private fun makeMint(record: SolanaBalanceRecord.MintToRecord) = MintActivityDto(
        id = record.id,
        date = record.timestamp,
        owner = record.account,
        tokenAddress = record.mint,
        value = record.mintAmount,
        blockchainInfo = blockchainInfo(record.log),
    )

    private fun makeBurn(record: SolanaBalanceRecord.BurnRecord) = BurnActivityDto(
        id = record.id,
        date = record.timestamp,
        owner = record.account,
        tokenAddress = record.mint,
        value = record.burnAmount,
        blockchainInfo = blockchainInfo(record.log),
    )

    private fun makeTransferIn(record: SolanaBalanceRecord.TransferIncomeRecord) = TransferActivityDto(
        id = record.id,
        date = record.timestamp,
        from = record.from,
        owner = record.owner,
        tokenAddress = record.mint,
        value = record.incomeAmount,
        blockchainInfo = blockchainInfo(record.log),
    )

    private fun makeTransferOut(record: SolanaBalanceRecord.TransferOutcomeRecord) = TransferActivityDto(
        id = record.id,
        date = record.timestamp,
        from = record.owner,
        owner = record.to,
        tokenAddress = record.mint,
        value = record.outcomeAmount,
        blockchainInfo = blockchainInfo(record.log),
    )

    private fun blockchainInfo(log: SolanaLog) = ActivityBlockchainInfoDto(
        blockNumber = log.blockNumber,
        blockHash = log.blockHash,
        transactionIndex = log.transactionIndex,
        transactionHash = log.transactionHash,
        instructionIndex = log.instructionIndex,
        innerInstructionIndex = log.innerInstructionIndex,
    )

    companion object {
        private const val DEFAULT_SIZE = 50
    }
}
