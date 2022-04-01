package com.rarible.protocol.solana.common.update

import com.rarible.core.kafka.RaribleKafkaProducer
import com.rarible.protocol.solana.dto.ActivityDto
import org.springframework.stereotype.Component

@Component
class ActivityEventListener(
    private val publisher: RaribleKafkaProducer<ActivityDto>
) {

    suspend fun onActivities(activities: List<ActivityDto>) {
        val messages = activities.map { KafkaEventFactory.activityEvent(it) }
        publisher.send(messages).collect { it.ensureSuccess() }
    }

}