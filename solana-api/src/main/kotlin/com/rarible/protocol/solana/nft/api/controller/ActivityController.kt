package com.rarible.protocol.solana.nft.api.controller

import com.rarible.protocol.solana.api.controller.ActivityControllerApi
import com.rarible.protocol.solana.dto.ActivitiesDto
import com.rarible.protocol.solana.dto.ActivityFilterAllDto
import com.rarible.protocol.solana.dto.ActivityFilterByCollectionDto
import com.rarible.protocol.solana.dto.ActivityFilterByItemDto
import com.rarible.protocol.solana.dto.ActivityFilterByUserDto
import com.rarible.protocol.solana.dto.ActivityFilterDto
import com.rarible.protocol.solana.dto.ActivitySortDto
import com.rarible.protocol.solana.nft.api.service.ActivityApiService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class ActivityController(
    private val activityApiService: ActivityApiService,
) : ActivityControllerApi {

    private val maxSize = 1000
    private val defaultSize = 50
    private val defaultSort = ActivitySortDto.LATEST_FIRST

    override suspend fun searchActivities(
        filter: ActivityFilterDto,
        continuation: String?,
        size: Int?,
        sort: ActivitySortDto?
    ): ResponseEntity<ActivitiesDto> {

        val safeSort = sort ?: defaultSort
        val safeSize = Math.min(size ?: defaultSize, maxSize)

        val result = when (filter) {
            is ActivityFilterAllDto -> activityApiService.getAllActivities(
                filter, continuation, safeSize, safeSort
            )
            is ActivityFilterByItemDto -> activityApiService.getActivitiesByItem(
                filter, continuation, safeSize, safeSort
            )
            is ActivityFilterByCollectionDto -> activityApiService.getActivitiesByCollection(
                filter, continuation, safeSize, safeSort
            )
            is ActivityFilterByUserDto -> activityApiService.getActivitiesByUser(
                filter, continuation, safeSize, safeSort
            )
        }
        return ResponseEntity.ok(result)
    }
}
