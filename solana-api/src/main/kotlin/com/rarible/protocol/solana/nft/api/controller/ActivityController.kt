package com.rarible.protocol.solana.nft.api.controller

import com.rarible.protocol.solana.api.controller.ActivityControllerApi
import com.rarible.protocol.solana.common.continuation.ActivityContinuation
import com.rarible.protocol.solana.common.continuation.IdContinuation
import com.rarible.protocol.solana.common.continuation.DateIdContinuation
import com.rarible.protocol.solana.common.continuation.Paging
import com.rarible.protocol.solana.dto.ActivitiesByIdRequestDto
import com.rarible.protocol.solana.dto.ActivitiesDto
import com.rarible.protocol.solana.dto.ActivityDto
import com.rarible.protocol.solana.dto.ActivityFilterAllDto
import com.rarible.protocol.solana.dto.ActivityFilterByCollectionDto
import com.rarible.protocol.solana.dto.ActivityFilterByItemDto
import com.rarible.protocol.solana.dto.ActivityFilterByUserDto
import com.rarible.protocol.solana.dto.ActivityFilterDto
import com.rarible.protocol.solana.dto.ActivitySortDto
import com.rarible.protocol.solana.nft.api.service.ActivityApiService
import com.rarible.protocol.union.dto.continuation.page.PageSize
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class ActivityController(
    private val activityApiService: ActivityApiService
) : ActivityControllerApi {

    override suspend fun searchActivities(
        activityFilterDto: ActivityFilterDto,
        continuation: String?,
        size: Int?,
        sort: ActivitySortDto?,
    ): ResponseEntity<ActivitiesDto> {

        val sortAscending = sort == ActivitySortDto.EARLIEST_FIRST
        val safeSize = PageSize.ACTIVITY.limit(size)

        val idContinuation = continuation?.let { parseContinuation(it) }

        val result = when (activityFilterDto) {
            is ActivityFilterAllDto -> activityApiService.getAllActivities(
                activityFilterDto, idContinuation, safeSize, sortAscending
            )
            is ActivityFilterByItemDto -> activityApiService.getActivitiesByItem(
                activityFilterDto, idContinuation, safeSize, sortAscending
            )
            is ActivityFilterByCollectionDto -> activityApiService.getActivitiesByCollection(
                activityFilterDto, idContinuation, safeSize, sortAscending
            )
            is ActivityFilterByUserDto -> activityApiService.getActivitiesByUser(
                activityFilterDto, idContinuation, safeSize, sortAscending
            )
        }

        val dto = toSlice(result, sortAscending, safeSize)

        return ResponseEntity.ok(dto)
    }

    private fun parseContinuation(continuation: String): IdContinuation {
        // Union by default uses the DateIdContinuation.
        //  We don't need the date part at all, so converting to just IdContinuation.
        val asDateIdContinuation = DateIdContinuation.parse(continuation)
        if (asDateIdContinuation != null) {
            return IdContinuation(asDateIdContinuation.id)
        }
        return IdContinuation(continuation)
    }

    override suspend fun searchActivitiesByIds(activitiesByIdRequestDto: ActivitiesByIdRequestDto): ResponseEntity<ActivitiesDto> {
        val activities = activityApiService.getActivitiesByIds(activitiesByIdRequestDto.ids)
        return ResponseEntity.ok(ActivitiesDto(continuation = null, activities = activities))
    }

    private fun toSlice(result: List<ActivityDto>, asc: Boolean, size: Int): ActivitiesDto {
        val continuationFactory = ActivityContinuation.ById(asc)
        val slice = Paging(continuationFactory, result).getSlice(size)
        return ActivitiesDto(slice.continuation, slice.entities)
    }
}
