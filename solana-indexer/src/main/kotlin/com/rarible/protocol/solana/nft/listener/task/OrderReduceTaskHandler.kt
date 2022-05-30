package com.rarible.protocol.solana.nft.listener.task

import com.rarible.core.entity.reducer.service.StreamFullReduceService
import com.rarible.core.task.TaskHandler
import com.rarible.protocol.solana.common.converter.SolanaAuctionHouseOrderActivityConverter
import com.rarible.protocol.solana.common.event.OrderEvent
import com.rarible.protocol.solana.common.model.Order
import com.rarible.protocol.solana.common.model.OrderId
import com.rarible.protocol.solana.common.records.SolanaAuctionHouseOrderRecord
import com.rarible.protocol.solana.common.repository.ActivityRepository
import com.rarible.protocol.solana.common.repository.SolanaAuctionHouseOrderRecordsRepository
import com.rarible.protocol.solana.common.update.ActivityEventListener
import com.rarible.protocol.solana.nft.listener.service.order.OrderEventConverter
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

typealias OrderStreamFullReduceService = StreamFullReduceService<OrderId, OrderEvent, Order>

@Component
class OrderReduceTaskHandler(
    private val orderStreamFullReduceService: OrderStreamFullReduceService,
    private val orderRecordsRepository: SolanaAuctionHouseOrderRecordsRepository,
    private val orderEventConverter: OrderEventConverter,
    private val orderActivityConverter: SolanaAuctionHouseOrderActivityConverter,
    private val activityEventListener: ActivityEventListener
) : TaskHandler<String> {
    override val type: String = "ORDER_REDUCER"

    @Suppress("EXPERIMENTAL_API_USAGE", "OPT_IN_USAGE")
    override fun runLongTask(from: String?, param: String): Flow<String> {
        logger.info("Starting $type with from: $from, param: $param")

        val criteria = if (param.isNotBlank()) {
            val auctionHouse = param.substringBefore(":")
            val orderId = param.substringAfter(":")

            require(auctionHouse.isNotBlank()) { "Auction house must be specified" }
            Criteria.where(SolanaAuctionHouseOrderRecord::auctionHouse.name).`is`(auctionHouse).andOperator(
                when {
                    from != null -> Criteria.where(SolanaAuctionHouseOrderRecord::orderId.name).gt(from.substringAfter(":"))
                    orderId.isNotBlank() -> Criteria.where(SolanaAuctionHouseOrderRecord::orderId.name).`is`(orderId)
                    else -> Criteria()
                }
            )
        } else {
            if (from == null) {
                Criteria()
            } else {
                val fromAuctionHouse = param.substringBefore(":")
                val fromOrderId = param.substringAfter(":")

                Criteria.where(SolanaAuctionHouseOrderRecord::auctionHouse.name).gte(fromAuctionHouse).andOperator(
                    Criteria.where(SolanaAuctionHouseOrderRecord::orderId.name).gt(fromOrderId)
                )
            }
        }
        val orderFlow = orderRecordsRepository.findBy(
            criteria,
            Sort.by(
                Sort.Direction.ASC,
                SolanaAuctionHouseOrderRecord::auctionHouse.name,
                SolanaAuctionHouseOrderRecord::orderId.name,
                "_id",
            )
        ).onEach { saveOrderActivity(it) }.flatMapConcat {
            orderEventConverter.convert(it, false).asFlow()
        }

        return orderStreamFullReduceService.reduce(orderFlow).map { "${it.auctionHouse}:${it.id}" }
    }

    private suspend fun saveOrderActivity(orderRecord: SolanaAuctionHouseOrderRecord) {
        val activityDto = orderActivityConverter.convert(orderRecord, false)
        if (activityDto != null) {
            activityEventListener.onActivities(listOf(activityDto))
        }
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(OrderReduceTaskHandler::class.java)
    }
}