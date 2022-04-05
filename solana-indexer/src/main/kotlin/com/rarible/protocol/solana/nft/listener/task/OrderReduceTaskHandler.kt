package com.rarible.protocol.solana.nft.listener.task

import com.rarible.core.entity.reducer.service.StreamFullReduceService
import com.rarible.core.task.TaskHandler
import com.rarible.protocol.solana.common.event.OrderEvent
import com.rarible.protocol.solana.common.model.Order
import com.rarible.protocol.solana.common.model.OrderId
import com.rarible.protocol.solana.common.records.SolanaAuctionHouseOrderRecord
import com.rarible.protocol.solana.common.repository.SolanaAuctionHouseOrderRecordsRepository
import com.rarible.protocol.solana.nft.listener.service.order.OrderEventConverter
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.stereotype.Component

typealias OrderStreamFullReduceService = StreamFullReduceService<OrderId, OrderEvent, Order>

@Component
class OrderReduceTaskHandler(
    private val orderStreamFullReduceService: OrderStreamFullReduceService,
    private val orderRecordsRepository: SolanaAuctionHouseOrderRecordsRepository,
    private val orderEventConverter: OrderEventConverter
) : TaskHandler<String> {
    override val type: String = "ORDER_REDUCER"

    @Suppress("EXPERIMENTAL_API_USAGE")
    override fun runLongTask(from: String?, param: String) = flow {
        BalanceReduceTaskHandler.logger.info("Starting $type with from: $from, param: $param")
        require(param.isNotBlank()) { "Auction house must be specified" }
        val criteria = Criteria.where(SolanaAuctionHouseOrderRecord::auctionHouse.name).`is`(param)
        val orderFlow = orderRecordsRepository.findBy(
            if (from != null) criteria.and(SolanaAuctionHouseOrderRecord::orderId.name).gt(from) else criteria,
            Sort.by(
                Sort.Direction.ASC,
                SolanaAuctionHouseOrderRecord::auctionHouse.name,
                SolanaAuctionHouseOrderRecord::orderId.name,
                SolanaAuctionHouseOrderRecord::id.name,
            )
        ).flatMapConcat {
            orderEventConverter.convert(it, false).asFlow()
        }

        orderStreamFullReduceService.reduce(orderFlow).map { emit(it.id) }
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(OrderReduceTaskHandler::class.java)
    }
}