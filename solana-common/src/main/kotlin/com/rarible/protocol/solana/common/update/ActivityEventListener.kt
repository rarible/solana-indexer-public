package com.rarible.protocol.solana.common.update

import com.rarible.core.kafka.RaribleKafkaProducer
import com.rarible.protocol.solana.common.repository.ActivityRepository
import com.rarible.protocol.solana.dto.ActivityDto
import org.springframework.stereotype.Component

@Component
class ActivityEventListener(
    private val publisher: RaribleKafkaProducer<ActivityDto>,
    private val activityRepository: ActivityRepository
) {

    suspend fun onActivities(
        activities: List<ActivityDto>,
        alternativeCollection: String? = null
    ) {
        val (revertedActivities, newActivities) = activities.partition { it.reverted }

        // Remove only from the main collection.
        activityRepository.removeByIds(revertedActivities.map { it.id })

        if (alternativeCollection != null) {
            activityRepository.insertAll(newActivities, alternativeCollection)
        } else {
            activityRepository.saveAll(newActivities)
        }

        val messages = activities.map { KafkaEventFactory.activityEvent(it) }
        publisher.send(messages).collect { it.ensureSuccess() }
    }

}