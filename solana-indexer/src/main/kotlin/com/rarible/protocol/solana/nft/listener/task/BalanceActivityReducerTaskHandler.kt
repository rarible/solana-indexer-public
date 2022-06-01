package com.rarible.protocol.solana.nft.listener.task

import com.rarible.core.kafka.chunked
import com.rarible.core.task.TaskHandler
import com.rarible.protocol.solana.common.converter.SolanaBalanceActivityConverter
import com.rarible.protocol.solana.common.repository.ActivityRepository
import com.rarible.protocol.solana.common.repository.SolanaBalanceRecordsRepository
import com.rarible.protocol.solana.common.update.ActivityEventListener
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.stereotype.Component

/**
 * Background task used to save balance activities to 'activity' table and send Kafka updates for them.
 * Note that order's activities are sent along with the full orders reduce.
 * For the balances it takes too much time, so we decouple reducing of the balances themselves, and saving their activities.
 */
@Component
class BalanceActivityReducerTaskHandler(
    private val balanceRecordsRepository: SolanaBalanceRecordsRepository,
    private val balanceActivityConverter: SolanaBalanceActivityConverter,
    private val activityEventListener: ActivityEventListener
) : TaskHandler<String> {

    private val logger = LoggerFactory.getLogger(BalanceActivityReducerTaskHandler::class.java)

    override val type: String = "BALANCE_ACTIVITY_SAVER"

    override fun runLongTask(from: String?, param: String): Flow<String> {
        val (toId, alternativeCollectionName) = when {
            param.contains(":") -> param.substringBefore(":") to param.substringAfter(":")
            param.isNotBlank() -> param to null
            else -> null to null
        }

        logger.info("Saving activities from '$from' to '$toId' to collection '${alternativeCollectionName ?: ActivityRepository.COLLECTION}'")
        val criteria = when {
            from != null && toId != null -> Criteria().andOperator(
                Criteria.where("_id").gt(from),
                Criteria.where("_id").lte(toId),
            )
            from != null -> Criteria.where("_id").gt(from)
            else -> Criteria()
        }

        return balanceRecordsRepository.findBy(
            criteria = criteria,
            sort = Sort.by(Sort.Direction.ASC, "_id"),
        ).chunked(1000, 1000).map { recordsChunk ->
            val activities = coroutineScope {
                recordsChunk.map { balanceRecord ->
                    async {
                        balanceActivityConverter.convert(balanceRecord, false)
                    }
                }.awaitAll().filterNotNull()
            }
            activityEventListener.onActivities(activities, alternativeCollection = alternativeCollectionName)
            recordsChunk.last().id
        }
    }

}