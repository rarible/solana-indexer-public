package com.rarible.protocol.solana.nft.api.controller

import com.rarible.protocol.solana.api.controller.ActivityControllerApi
import com.rarible.protocol.solana.common.continuation.ActivityContinuation
import com.rarible.protocol.solana.common.continuation.DateIdContinuation
import com.rarible.protocol.solana.common.continuation.Paging
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
    private val activityApiService: ActivityApiService,
) : ActivityControllerApi {

    override suspend fun searchActivities(
        filter: ActivityFilterDto,
        continuation: String?,
        size: Int?,
        sort: ActivitySortDto?,
    ): ResponseEntity<ActivitiesDto> {

        val sortAscending = sort == ActivitySortDto.EARLIEST_FIRST
        val safeSize = PageSize.ACTIVITY.limit(size)

        val dateIdContinuation = DateIdContinuation.parse(continuation)

        val result = when (filter) {
            is ActivityFilterAllDto -> activityApiService.getAllActivities(
                filter, dateIdContinuation, safeSize, sortAscending
            )
            is ActivityFilterByItemDto -> activityApiService.getActivitiesByItem(
                filter, dateIdContinuation, safeSize, sortAscending
            )
            is ActivityFilterByCollectionDto -> activityApiService.getActivitiesByCollection(
                filter, dateIdContinuation, safeSize, sortAscending
            )
            is ActivityFilterByUserDto -> activityApiService.getActivitiesByUser(
                filter, dateIdContinuation, safeSize, sortAscending
            )
        }

        val dto = toSlice(result, sortAscending, safeSize)

        return ResponseEntity.ok(dto)
    }

    private fun toSlice(result: List<ActivityDto>, asc: Boolean, size: Int): ActivitiesDto {
        val continuationFactory =
            if (asc) ActivityContinuation.ByLastUpdatedAndIdAsc
            else ActivityContinuation.ByLastUpdatedAndIdDesc

        val slice = Paging(continuationFactory, result).getSlice(size)
        return ActivitiesDto(slice.continuation, slice.entities)
    }
}
