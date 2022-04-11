package com.rarible.protocol.solana.nft.listener.task

import com.rarible.core.entity.reducer.service.StreamFullReduceService
import com.rarible.core.task.TaskHandler
import com.rarible.protocol.solana.common.event.MetaplexMetaEvent
import com.rarible.protocol.solana.common.model.MetaId
import com.rarible.protocol.solana.common.model.MetaplexMeta
import com.rarible.protocol.solana.common.records.SolanaMetaRecord
import com.rarible.protocol.solana.common.repository.SolanaMetaplexMetaRecordsRepository
import com.rarible.protocol.solana.nft.listener.service.meta.MetaEventConverter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.stereotype.Component

typealias MetaplexMetaStreamFullReduceService = StreamFullReduceService<MetaId, MetaplexMetaEvent, MetaplexMeta>

@Component
class MetaplexMetaReduceTaskHandler(
    private val metaStreamFullReduceService: MetaplexMetaStreamFullReduceService,
    private val metaplexMetaRecordsRepository: SolanaMetaplexMetaRecordsRepository,
    private val metaEventConverter: MetaEventConverter
) : TaskHandler<String> {
    override val type: String = "METAPLEX_META_REDUCER"

    @Suppress("EXPERIMENTAL_API_USAGE", "OPT_IN_USAGE")
    override fun runLongTask(from: String?, param: String) : Flow<String> {
        logger.info("Starting $type with from: $from, param: $param")

        val criteria = when {
            from != null -> Criteria.where(SolanaMetaRecord::metaAccount.name).gt(from)
            param.isNotBlank() -> Criteria.where(SolanaMetaRecord::metaAccount.name).`is`(param)
            else -> Criteria()
        }
        val metaEventFlow = metaplexMetaRecordsRepository.findBy(
            criteria = criteria,
            sort = Sort.by(Sort.Direction.ASC, SolanaMetaRecord::metaAccount.name, "_id"),
        ).flatMapConcat {
            metaEventConverter.convert(it, false).asFlow()
        }

        return metaStreamFullReduceService.reduce(metaEventFlow).map { it.metaAddress }
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(MetaplexMetaReduceTaskHandler::class.java)
    }
}