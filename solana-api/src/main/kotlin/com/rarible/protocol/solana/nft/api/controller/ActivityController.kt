package com.rarible.protocol.solana.nft.api.controller

import com.rarible.protocol.solana.nft.api.service.ActivityApiService
import com.rarible.solana.protocol.api.controller.ActivityControllerApi
import com.rarible.solana.protocol.dto.ActivitiesDto
import com.rarible.solana.protocol.dto.ActivitySortDto
import com.rarible.solana.protocol.dto.ActivityTypeDto
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class ActivityController(
    private val activityApiService: ActivityApiService,
) : ActivityControllerApi {
    override suspend fun getActivitiesByCollection(
        type: List<ActivityTypeDto>,
        collection: String,
        continuation: String?,
        size: Int?,
        sort: ActivitySortDto?,
    ): ResponseEntity<ActivitiesDto> {
        val activities = activityApiService.getActivitiesByCollection(
            type, collection, continuation, size,
            sort ?: ActivitySortDto.LATEST_FIRST
        )
        return ResponseEntity.ok(activities)
    }

    override suspend fun getActivitiesByItem(
        type: List<ActivityTypeDto>,
        tokenAddress: String,
        continuation: String?,
        size: Int?,
        sort: ActivitySortDto?,
    ): ResponseEntity<ActivitiesDto> {
        val activities = activityApiService.getActivitiesByItem(
            type, tokenAddress, continuation, size,
            sort ?: ActivitySortDto.LATEST_FIRST
        )
        return ResponseEntity.ok(activities)
    }

    override suspend fun getAllActivities(
        type: List<ActivityTypeDto>,
        continuation: String?,
        size: Int?,
        sort: ActivitySortDto?,
    ): ResponseEntity<ActivitiesDto> {
        val activities = activityApiService.getAllActivities(
            type, continuation, size,
            sort ?: ActivitySortDto.LATEST_FIRST
        )
        return ResponseEntity.ok(activities)
    }
}
