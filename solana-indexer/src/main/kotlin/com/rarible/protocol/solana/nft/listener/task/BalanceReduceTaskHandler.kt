package com.rarible.protocol.solana.nft.listener.task

import com.rarible.core.entity.reducer.service.StreamFullReduceService
import com.rarible.core.task.TaskHandler
import com.rarible.protocol.solana.common.event.BalanceEvent
import com.rarible.protocol.solana.common.model.Balance
import com.rarible.protocol.solana.common.model.BalanceId
import com.rarible.protocol.solana.common.records.SolanaBalanceRecord
import com.rarible.protocol.solana.common.repository.SolanaBalanceRecordsRepository
import com.rarible.protocol.solana.nft.listener.service.balance.BalanceEventConverter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
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
    private val balanceEventConverter: BalanceEventConverter
) : TaskHandler<String> {
    override val type: String = "BALANCE_REDUCER"

    @Suppress("EXPERIMENTAL_API_USAGE")
    override fun runLongTask(from: String?, param: String) : Flow<String> {
        logger.info("Starting $type with from: $from, param: $param")

        val criteria = if (param.isNotBlank()) {
            Criteria.where(SolanaBalanceRecord::account.name).`is`(param)
        } else {
            Criteria()
        }
        val balanceFlow = balanceRecordsRepository.findBy(
            if (from != null) criteria.and(SolanaBalanceRecord::account.name).gt(from) else criteria,
            Sort.by(Sort.Direction.ASC, SolanaBalanceRecord::account.name, SolanaBalanceRecord::id.name),
        ).flatMapConcat {
            balanceEventConverter.convert(it, false).asFlow()
        }


        return balanceStreamFullReduceService.reduce(balanceFlow).map { it.account }
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(BalanceReduceTaskHandler::class.java)
    }
}