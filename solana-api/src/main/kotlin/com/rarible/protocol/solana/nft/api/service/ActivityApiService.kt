package com.rarible.protocol.solana.nft.api.service

import com.rarible.blockchain.scanner.solana.model.SolanaLog
import com.rarible.protocol.solana.common.continuation.DateIdContinuation
import com.rarible.protocol.solana.common.records.SolanaBalanceRecord
import com.rarible.protocol.solana.common.repository.RecordsBalanceRepository
import com.rarible.protocol.solana.dto.ActivityBlockchainInfoDto
import com.rarible.protocol.solana.dto.ActivityDto
import com.rarible.protocol.solana.dto.ActivityFilterAllDto
import com.rarible.protocol.solana.dto.ActivityFilterAllTypeDto
import com.rarible.protocol.solana.dto.ActivityFilterByCollectionDto
import com.rarible.protocol.solana.dto.ActivityFilterByCollectionTypeDto
import com.rarible.protocol.solana.dto.ActivityFilterByItemDto
import com.rarible.protocol.solana.dto.ActivityFilterByItemTypeDto
import com.rarible.protocol.solana.dto.ActivityFilterByUserDto
import com.rarible.protocol.solana.dto.ActivitySortDto
import com.rarible.protocol.solana.dto.ActivityTypeDto
import com.rarible.protocol.solana.dto.BurnActivityDto
import com.rarible.protocol.solana.dto.MintActivityDto
import com.rarible.protocol.solana.dto.TransferActivityDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Component

@Component
class ActivityApiService(
    private val recordsBalanceRepository: RecordsBalanceRepository,
) {

    suspend fun getAllActivities(
        filter: ActivityFilterAllDto,
        continuation: DateIdContinuation?,
        size: Int,
        sort: ActivitySortDto,
    ) = getActivities(size) { actualSize ->
        recordsBalanceRepository.findAll(
            filter.types.map { convert(it) },
            continuation,
            actualSize,
            sort
        )
    }

    suspend fun getActivitiesByItem(
        filter: ActivityFilterByItemDto,
        continuation: DateIdContinuation?,
        size: Int,
        sort: ActivitySortDto,
    ) = getActivities(size) { actualSize ->
        recordsBalanceRepository.findByItem(
            filter.types.map { convert(it) },
            filter.itemId,
            continuation,
            actualSize,
            sort
        )
    }

    suspend fun getActivitiesByCollection(
        filter: ActivityFilterByCollectionDto,
        continuation: DateIdContinuation?,
        size: Int,
        sort: ActivitySortDto,
    ) = getActivities(size) { actualSize ->
        recordsBalanceRepository.findByCollection(
            filter.types.map { convert(it) },
            filter.collection,
            continuation,
            actualSize,
            sort
        )
    }

    suspend fun getActivitiesByUser(
        filter: ActivityFilterByUserDto,
        continuation: DateIdContinuation?,
        size: Int,
        sort: ActivitySortDto,
    ) = getActivities(size) { actualSize ->
        emptyFlow()
    }

    private suspend fun getActivities(size: Int, block: (Int) -> Flow<SolanaBalanceRecord>): List<ActivityDto> {
        return block(size).mapNotNull { convert(it) }.toList()
    }

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
        reverted = false
    )

    private fun makeBurn(record: SolanaBalanceRecord.BurnRecord) = BurnActivityDto(
        id = record.id,
        date = record.timestamp,
        owner = record.account,
        tokenAddress = record.mint,
        value = record.burnAmount,
        blockchainInfo = blockchainInfo(record.log),
        reverted = false
    )

    private fun makeTransferIn(record: SolanaBalanceRecord.TransferIncomeRecord) = TransferActivityDto(
        id = record.id,
        date = record.timestamp,
        from = record.from,
        owner = record.owner,
        tokenAddress = record.mint,
        value = record.incomeAmount,
        blockchainInfo = blockchainInfo(record.log),
        reverted = false,
        purchase = false // TODO should be evaluated
    )

    private fun makeTransferOut(record: SolanaBalanceRecord.TransferOutcomeRecord) = TransferActivityDto(
        id = record.id,
        date = record.timestamp,
        from = record.owner,
        owner = record.to,
        tokenAddress = record.mint,
        value = record.outcomeAmount,
        blockchainInfo = blockchainInfo(record.log),
        reverted = false,
        purchase = false // TODO should be evaluated
    )

    private fun blockchainInfo(log: SolanaLog) = ActivityBlockchainInfoDto(
        blockNumber = log.blockNumber,
        blockHash = log.blockHash,
        transactionIndex = log.transactionIndex,
        transactionHash = log.transactionHash,
        instructionIndex = log.instructionIndex,
        innerInstructionIndex = log.innerInstructionIndex,
    )

    private fun convert(type: ActivityFilterByCollectionTypeDto): ActivityTypeDto {
        return when (type) {
            ActivityFilterByCollectionTypeDto.TRANSFER -> ActivityTypeDto.TRANSFER
            ActivityFilterByCollectionTypeDto.MINT -> ActivityTypeDto.MINT
            ActivityFilterByCollectionTypeDto.BURN -> ActivityTypeDto.BURN
            ActivityFilterByCollectionTypeDto.BID -> ActivityTypeDto.BID
            ActivityFilterByCollectionTypeDto.LIST -> ActivityTypeDto.LIST
            ActivityFilterByCollectionTypeDto.SELL -> ActivityTypeDto.SELL
            ActivityFilterByCollectionTypeDto.CANCEL_BID -> ActivityTypeDto.CANCEL_BID
            ActivityFilterByCollectionTypeDto.CANCEL_LIST -> ActivityTypeDto.CANCEL_LIST
        }
    }

    private fun convert(type: ActivityFilterByItemTypeDto): ActivityTypeDto {
        return when (type) {
            ActivityFilterByItemTypeDto.TRANSFER -> ActivityTypeDto.TRANSFER
            ActivityFilterByItemTypeDto.MINT -> ActivityTypeDto.MINT
            ActivityFilterByItemTypeDto.BURN -> ActivityTypeDto.BURN
            ActivityFilterByItemTypeDto.BID -> ActivityTypeDto.BID
            ActivityFilterByItemTypeDto.LIST -> ActivityTypeDto.LIST
            ActivityFilterByItemTypeDto.SELL -> ActivityTypeDto.SELL
            ActivityFilterByItemTypeDto.CANCEL_BID -> ActivityTypeDto.CANCEL_BID
            ActivityFilterByItemTypeDto.CANCEL_LIST -> ActivityTypeDto.CANCEL_LIST
        }
    }

    private fun convert(type: ActivityFilterAllTypeDto): ActivityTypeDto {
        return when (type) {
            ActivityFilterAllTypeDto.TRANSFER -> ActivityTypeDto.TRANSFER
            ActivityFilterAllTypeDto.MINT -> ActivityTypeDto.MINT
            ActivityFilterAllTypeDto.BURN -> ActivityTypeDto.BURN
            ActivityFilterAllTypeDto.BID -> ActivityTypeDto.BID
            ActivityFilterAllTypeDto.LIST -> ActivityTypeDto.LIST
            ActivityFilterAllTypeDto.SELL -> ActivityTypeDto.SELL
            ActivityFilterAllTypeDto.CANCEL_BID -> ActivityTypeDto.CANCEL_BID
            ActivityFilterAllTypeDto.CANCEL_LIST -> ActivityTypeDto.CANCEL_LIST
        }
    }

}
