package com.rarible.protocol.solana.nft.api.service

import com.rarible.protocol.solana.common.continuation.DateIdContinuation
import com.rarible.protocol.solana.common.repository.ActivityRepository
import com.rarible.protocol.solana.dto.ActivityDto
import com.rarible.protocol.solana.dto.ActivityFilterAllDto
import com.rarible.protocol.solana.dto.ActivityFilterAllTypeDto
import com.rarible.protocol.solana.dto.ActivityFilterByCollectionDto
import com.rarible.protocol.solana.dto.ActivityFilterByItemDto
import com.rarible.protocol.solana.dto.ActivityFilterByItemTypeDto
import com.rarible.protocol.solana.dto.ActivityFilterByUserDto
import com.rarible.protocol.solana.dto.ActivityTypeDto
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service

// TODO: remove after reindex of dev/e2e.
@Service
class ActivityApiServiceLegacy(
    private val activityRepository: ActivityRepository,
) {

    suspend fun getAllActivities(
        filter: ActivityFilterAllDto,
        continuation: DateIdContinuation?,
        size: Int,
        sortAscending: Boolean,
    ): List<ActivityDto> {
        val types = filter.types.map { convert(it) }
        if (types.isEmpty()) return emptyList()

        return activityRepository.findAllActivities(types, continuation, size, sortAscending)
            .take(size).toList()
    }

    suspend fun getActivitiesByItem(
        filter: ActivityFilterByItemDto,
        continuation: DateIdContinuation?,
        size: Int,
        sortAscending: Boolean,
    ): List<ActivityDto> {
        val types = filter.types.map { convert(it) }
        if (types.isEmpty()) return emptyList()

        return activityRepository.findActivitiesByMint(types, filter.itemId, continuation, size, sortAscending)
            .take(size).toList()
    }

    suspend fun getActivitiesByCollection(
        filter: ActivityFilterByCollectionDto,
        continuation: DateIdContinuation?,
        size: Int,
        sortAscending: Boolean,
    ) = emptyList<ActivityDto>() // TODO: not implemented yet.

    suspend fun getActivitiesByUser(
        filter: ActivityFilterByUserDto,
        continuation: DateIdContinuation?,
        size: Int,
        sortAscending: Boolean,
    ) = emptyList<ActivityDto>() // TODO: not implemented yet.


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
}
