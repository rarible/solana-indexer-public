package com.rarible.protocol.solana.nft.api.service

import com.rarible.protocol.solana.common.continuation.DateIdContinuation
import com.rarible.protocol.solana.common.converter.ActivityConverter
import com.rarible.protocol.solana.common.records.SolanaBalanceRecord
import com.rarible.protocol.solana.common.repository.RecordsBalanceRepository
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Component

@Component
class ActivityApiService(
    private val recordsBalanceRepository: RecordsBalanceRepository,
    private val activityConverter: ActivityConverter
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
    ) = getActivities(size) {
        emptyFlow()
    }

    private suspend fun getActivities(size: Int, block: (Int) -> Flow<SolanaBalanceRecord>): List<ActivityDto> {
        return block(size).mapNotNull { activityConverter.convert(it, false) }.toList()
    }

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
