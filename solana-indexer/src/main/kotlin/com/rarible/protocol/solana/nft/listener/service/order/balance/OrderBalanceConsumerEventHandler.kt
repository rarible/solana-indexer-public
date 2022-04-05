package com.rarible.protocol.solana.nft.listener.service.order.balance

import com.rarible.blockchain.scanner.framework.data.LogRecordEvent
import com.rarible.blockchain.scanner.publisher.LogRecordEventPublisher
import com.rarible.blockchain.scanner.solana.model.SolanaDescriptor
import com.rarible.core.daemon.sequential.ConsumerBatchEventHandler
import com.rarible.protocol.solana.common.records.SolanaAuctionHouseOrderRecord
import com.rarible.protocol.solana.common.records.SubscriberGroup
import com.rarible.protocol.solana.common.repository.OrderRepository
import com.rarible.protocol.solana.dto.BalanceDeleteEventDto
import com.rarible.protocol.solana.dto.BalanceEventDto
import com.rarible.protocol.solana.dto.BalanceUpdateEventDto
import com.rarible.protocol.solana.nft.listener.service.subscribers.SolanaProgramId
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class OrderBalanceConsumerEventHandler(
    private val orderRepository: OrderRepository,
    private val logRecordEventPublisher: LogRecordEventPublisher
) : ConsumerBatchEventHandler<BalanceEventDto> {

    private val logger = LoggerFactory.getLogger(javaClass)

    override suspend fun handle(event: List<BalanceEventDto>) {
        logger.info("Fetched ${event.size} order balance events: [${event.joinToString { it.account + ":" + it.mint }}]")
        event.forEach { balanceEvent ->
            val mint = balanceEvent.mint
            val orders = orderRepository.findBySellingNft(mint)
            orders.collect { order ->
                val fakeBalanceUpdateRecord = SolanaAuctionHouseOrderRecord.FakeBalanceUpdateRecord(
                    mint = mint,
                    timestamp = Instant.EPOCH, // TODO: maybe pass via the BalanceEvent,
                    auctionHouse = order.auctionHouse,
                    orderId = order.id
                )
                logRecordEventPublisher.publish(
                    groupId = SubscriberGroup.AUCTION_HOUSE_ORDER.id,
                    logRecordEvents = listOf(
                        LogRecordEvent(
                            record = fakeBalanceUpdateRecord,
                            reverted = false
                        )
                    )
                )
            }
        }
    }

    private val orderBalanceLogSolanaDescriptor = object : SolanaDescriptor(
        programId = SolanaProgramId.AUCTION_HOUSE_PROGRAM,
        id = "auction_house_order_balance_update",
        groupId = SubscriberGroup.AUCTION_HOUSE_ORDER.id,
        entityType = SolanaAuctionHouseOrderRecord.BuyRecord::class.java,
        collection = SubscriberGroup.AUCTION_HOUSE_ORDER.collectionName
    ) {}
}