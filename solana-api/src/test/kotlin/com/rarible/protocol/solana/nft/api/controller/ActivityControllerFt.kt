package com.rarible.protocol.solana.nft.api.controller

import com.rarible.protocol.solana.common.repository.ActivityRepository
import com.rarible.protocol.solana.dto.ActivitiesDto
import com.rarible.protocol.solana.dto.ActivityDto
import com.rarible.protocol.solana.dto.SyncSortDto
import com.rarible.protocol.solana.nft.api.test.AbstractControllerTest
import com.rarible.protocol.solana.test.randomBurn
import com.rarible.protocol.solana.test.randomMintActivity
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class ActivityControllerFt : AbstractControllerTest() {

    @Autowired
    lateinit var activityRepository: ActivityRepository

    @Test
    fun `activity controller sync test`() = runBlocking<Unit> {
        val activityQuantity = 5

        repeat(activityQuantity) {
            activityRepository.save(randomMintActivity())
        }

        val result: ActivitiesDto =
            activityControllerApi.getActivitiesSync(null, activityQuantity, SyncSortDto.DB_UPDATE_ASC).awaitFirst()

        Assertions.assertThat(result.activities).hasSize(activityQuantity)
        Assertions.assertThat(result.activities).isSortedAccordingTo { o1, o2 ->
            compareValues(
                o1.dbUpdatedAt,
                o2.dbUpdatedAt
            )
        }
    }

    @Test
    fun `activity controller sync pagination asc test `() = runBlocking<Unit> {
        val comparator = Comparator<ActivityDto> { o1, o2 -> compareValues(o1.dbUpdatedAt, o2.dbUpdatedAt) }

        testGetActivitiesSyncPagination(
            activityQuantity = 95,
            chunkSize = 20,
            sort = SyncSortDto.DB_UPDATE_ASC,
            comparator
        )
    }

      @Test
    fun `activity controller sync pagination des test `() = runBlocking<Unit> {
        val comparator = Comparator<ActivityDto> { o1, o2 -> compareValues(o2.dbUpdatedAt, o1.dbUpdatedAt) }

        testGetActivitiesSyncPagination(
            activityQuantity = 60,
            chunkSize = 20,
            sort = SyncSortDto.DB_UPDATE_DESC,
            comparator
        )
    }

    private suspend fun testGetActivitiesSyncPagination(
        activityQuantity: Int,
        chunkSize: Int,
        sort: SyncSortDto,
        comparator: Comparator<ActivityDto>
    ) {
        repeat(activityQuantity) {
            activityRepository.save(randomBurn())
        }

        var continuation: String? = null
        val activities = mutableListOf<ActivityDto>()
        var totalPages = 0

        do {
            val result =
                activityControllerApi.getActivitiesSync(continuation, chunkSize, sort).awaitFirst()
            activities.addAll(result.activities)
            continuation = result.continuation
            totalPages += 1
        } while (continuation != null)

        activities.forEach { println(it) }

        Assertions.assertThat(totalPages).isEqualTo(activityQuantity / chunkSize + 1)
        Assertions.assertThat(activities).hasSize(activityQuantity)
        Assertions.assertThat(activities).isSortedAccordingTo(comparator)
    }
}