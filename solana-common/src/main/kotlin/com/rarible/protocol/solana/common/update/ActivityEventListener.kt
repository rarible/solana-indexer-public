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

    suspend fun onActivities(activities: List<ActivityDto>) {
        for (activity in activities) {
            if (activity.reverted) {
                activityRepository.removeById(activity.id)
            } else {
                activityRepository.save(activity)
            }
        }
        val messages = activities.map { KafkaEventFactory.activityEvent(it) }
        publisher.send(messages).collect { it.ensureSuccess() }
    }

}