package com.rarible.protocol.solana.nft.listener.task

import com.rarible.core.kafka.RaribleKafkaProducer
import com.rarible.core.kafka.chunked
import com.rarible.core.task.TaskHandler
import com.rarible.protocol.solana.common.continuation.IdContinuation
import com.rarible.protocol.solana.common.repository.ActivityRepository
import com.rarible.protocol.solana.common.update.KafkaEventFactory
import com.rarible.protocol.solana.dto.ActivityDto
import com.rarible.protocol.solana.dto.ActivityTypeDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Background task used to send activities to Kafka.
 */
@Component
class ActivityToKafkaSenderTaskHandler(
    private val activityRepository: ActivityRepository,
    private val publisher: RaribleKafkaProducer<ActivityDto>
) : TaskHandler<String> {

    private val logger = LoggerFactory.getLogger(ActivityToKafkaSenderTaskHandler::class.java)

    override val type: String = "ACTIVITY_TO_KAFKA_SENDER"

    override fun runLongTask(from: String?, param: String): Flow<String> {
        logger.info("Sending activities from $from")
        val continuation = if (from != null) IdContinuation(from, true) else null
        return activityRepository.findAllActivities(
            types = ActivityTypeDto.values().toList(),
            continuation = continuation,
            size = null,
            sortAscending = true
        ).chunked(500, 1000).map { activitiesDto ->
            val messages = activitiesDto.map { KafkaEventFactory.activityEvent(it) }
            publisher.send(messages).collect { it.ensureSuccess() }
            activitiesDto.last().id
        }
    }

}