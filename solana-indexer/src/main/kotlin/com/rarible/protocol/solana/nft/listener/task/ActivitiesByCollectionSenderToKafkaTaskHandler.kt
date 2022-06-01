package com.rarible.protocol.solana.nft.listener.task

import com.rarible.core.kafka.RaribleKafkaProducer
import com.rarible.core.task.TaskHandler
import com.rarible.protocol.solana.common.meta.TokenMetaService
import com.rarible.protocol.solana.common.repository.ActivityRepository
import com.rarible.protocol.solana.common.update.KafkaEventFactory
import com.rarible.protocol.solana.dto.ActivityDto
import com.rarible.protocol.solana.dto.ActivityTypeDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Ad-hoc for CHARLIE-320: a background task to send activities only for a specific collection.
 * May be used by the data team to re-index activities for only one collection.
 */
@Component
class ActivitiesByCollectionSenderToKafkaTaskHandler(
    private val tokenMetaService: TokenMetaService,
    private val activityRepository: ActivityRepository,
    private val publisher: RaribleKafkaProducer<ActivityDto>,
) : TaskHandler<String> {

    private val logger = LoggerFactory.getLogger(ActivitiesByCollectionSenderToKafkaTaskHandler::class.java)

    override val type: String = "ACTIVITIES_BY_COLLECTION_SENDER_TO_KAFKA"

    override fun runLongTask(from: String?, param: String): Flow<String> {
        logger.info("Sending activities for all mints of collection '$param'")
        return flow {
            val tokensMeta = tokenMetaService.getTokensMetaByCollection(param, null, null)
            val mints = tokensMeta.keys
            mints.chunked(100).forEach { mintsChunk ->
                logger.info("Sending activities for collection '$param' for mints from '${mintsChunk.first()}' to '${mintsChunk.last()}'")
                val activitiesFlow = activityRepository.findActivitiesByMints(
                    types = ActivityTypeDto.values().toList(),
                    mints = mintsChunk,
                    continuation = null,
                    size = null,
                    sortAscending = true
                )
                publisher
                    .send(activitiesFlow.map { KafkaEventFactory.activityEvent(it) })
                    .collect { it.ensureSuccess() }
                emit(mintsChunk.last())
            }
        }
    }
}