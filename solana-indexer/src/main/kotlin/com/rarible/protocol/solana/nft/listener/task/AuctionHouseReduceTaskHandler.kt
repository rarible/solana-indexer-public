package com.rarible.protocol.solana.nft.listener.task

import com.rarible.core.entity.reducer.service.StreamFullReduceService
import com.rarible.core.task.TaskHandler
import com.rarible.protocol.solana.common.event.AuctionHouseEvent
import com.rarible.protocol.solana.common.model.AuctionHouse
import com.rarible.protocol.solana.common.model.AuctionHouseId
import com.rarible.protocol.solana.common.records.SolanaAuctionHouseRecord
import com.rarible.protocol.solana.common.repository.SolanaAuctionHouseRecordsRepository
import com.rarible.protocol.solana.nft.listener.service.auction.house.AuctionHouseEventConverter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.stereotype.Component

typealias AuctionHouseStreamFullReduceService = StreamFullReduceService<AuctionHouseId, AuctionHouseEvent, AuctionHouse>

@Component
class AuctionHouseReduceTaskHandler(
    private val auctionHouseStreamFullReduceService: AuctionHouseStreamFullReduceService,
    private val auctionHouseRecordsRepository: SolanaAuctionHouseRecordsRepository,
    private val auctionHouseEventConverter: AuctionHouseEventConverter
) : TaskHandler<String> {
    override val type: String = "AUCTION_HOUSE_REDUCER"

    @Suppress("EXPERIMENTAL_API_USAGE", "OPT_IN_USAGE")
    override fun runLongTask(from: String?, param: String): Flow<String> {
        logger.info("Starting $type with from: $from, param: $param")

        val criteria = when {
            from != null -> Criteria.where(SolanaAuctionHouseRecord::auctionHouse.name).gt(from)
            param.isNotBlank() -> Criteria.where(SolanaAuctionHouseRecord::auctionHouse.name).`is`(param)
            else -> Criteria()
        }
        val auctionHouseFlow = auctionHouseRecordsRepository.findBy(
            criteria = criteria,
            sort = Sort.by(Sort.Direction.ASC, SolanaAuctionHouseRecord::auctionHouse.name, "_id"),
        ).flatMapConcat {
            auctionHouseEventConverter.convert(it, false).asFlow()
        }

        return auctionHouseStreamFullReduceService.reduce(auctionHouseFlow).map { it.id }
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(AuctionHouseReduceTaskHandler::class.java)
    }
}