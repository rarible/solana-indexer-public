package com.rarible.protocol.solana.nft.listener.task

import com.rarible.core.entity.reducer.service.StreamFullReduceService
import com.rarible.core.task.TaskHandler
import com.rarible.protocol.solana.common.event.TokenEvent
import com.rarible.protocol.solana.common.model.Token
import com.rarible.protocol.solana.common.model.TokenId
import com.rarible.protocol.solana.common.records.SolanaTokenRecord
import com.rarible.protocol.solana.common.repository.SolanaTokenRecordsRepository
import com.rarible.protocol.solana.nft.listener.service.token.TokenEventConverter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.stereotype.Component

typealias TokenStreamFullReduceService = StreamFullReduceService<TokenId, TokenEvent, Token>

@Component
class TokenReduceTaskHandler(
    private val tokenStreamFullReduceService: TokenStreamFullReduceService,
    private val tokenRecordsRepository: SolanaTokenRecordsRepository,
    private val tokenEventConverter: TokenEventConverter
) : TaskHandler<String> {
    override val type: String = "TOKEN_REDUCER"

    @Suppress("EXPERIMENTAL_API_USAGE", "OPT_IN_USAGE")
    override fun runLongTask(from: String?, param: String): Flow<String> {
        logger.info("Starting $type with from: $from, param: $param")

        val criteria = when {
            from != null && param.isNotBlank() -> Criteria().andOperator(
                Criteria.where(SolanaTokenRecord::mint.name).gt(from),
                Criteria.where(SolanaTokenRecord::mint.name).lte(param),
            )
            param.isNotBlank() -> Criteria.where(SolanaTokenRecord::mint.name).`is`(param)
            else -> Criteria()
        }
        val tokenFlow = tokenRecordsRepository.findBy(
            criteria = criteria,
            sort = Sort.by(Sort.Direction.ASC, SolanaTokenRecord::mint.name, "_id"),
        ).flatMapConcat {
            tokenEventConverter.convert(it, false).asFlow()
        }

        return tokenStreamFullReduceService.reduce(tokenFlow).map { it.mint }
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(TokenReduceTaskHandler::class.java)
    }
}