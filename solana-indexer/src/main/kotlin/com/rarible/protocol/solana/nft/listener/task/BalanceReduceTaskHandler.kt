package com.rarible.protocol.solana.nft.listener.task

import com.rarible.core.entity.reducer.service.StreamFullReduceService
import com.rarible.core.task.TaskHandler
import com.rarible.protocol.solana.common.converter.SolanaBalanceActivityConverter
import com.rarible.protocol.solana.common.event.BalanceEvent
import com.rarible.protocol.solana.common.model.Balance
import com.rarible.protocol.solana.common.model.BalanceId
import com.rarible.protocol.solana.common.records.SolanaBalanceRecord
import com.rarible.protocol.solana.common.repository.ActivityRepository
import com.rarible.protocol.solana.common.repository.SolanaBalanceRecordsRepository
import com.rarible.protocol.solana.nft.listener.service.balance.BalanceEventConverter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.stereotype.Component

typealias BalanceStreamFullReduceService = StreamFullReduceService<BalanceId, BalanceEvent, Balance>

@Component
class BalanceReduceTaskHandler(
    private val balanceStreamFullReduceService: BalanceStreamFullReduceService,
    private val balanceRecordsRepository: SolanaBalanceRecordsRepository,
    private val balanceEventConverter: BalanceEventConverter,
    private val activityRepository: ActivityRepository,
    private val balanceActivityConverter: SolanaBalanceActivityConverter
) : TaskHandler<String> {
    override val type: String = "BALANCE_REDUCER"

    @Suppress("EXPERIMENTAL_API_USAGE", "OPT_IN_USAGE")
    override fun runLongTask(from: String?, param: String): Flow<String> {
        logger.info("Starting $type with from: $from, param: $param")

        val criteria = when {
            from != null && param.isNotBlank() -> {
                Criteria.where(SolanaBalanceRecord::account.name).gt(from)
                    .and(SolanaBalanceRecord::account.name).lte(param)
            }
            param.isNotBlank() -> Criteria.where(SolanaBalanceRecord::account.name).`is`(param)
            else -> Criteria()
        }
        val balanceFlow = balanceRecordsRepository.findBy(
            criteria,
            Sort.by(Sort.Direction.ASC, SolanaBalanceRecord::account.name, "_id"),
        ).onEach { saveBalanceActivity(it) }.flatMapConcat {
            balanceEventConverter.convert(it, false).asFlow()
        }


        return balanceStreamFullReduceService.reduce(balanceFlow).map { it.account }
    }

    // TODO: ideally, we should also remove all redundant left-over activities by this entity.
    private suspend fun saveBalanceActivity(balanceRecord: SolanaBalanceRecord) {
        val activityDto = balanceActivityConverter.convert(balanceRecord, false)
        if (activityDto != null) {
            activityRepository.save(activityDto)
        }
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(BalanceReduceTaskHandler::class.java)
    }
}