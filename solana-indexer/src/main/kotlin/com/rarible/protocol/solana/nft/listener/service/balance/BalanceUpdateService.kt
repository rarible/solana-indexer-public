package com.rarible.protocol.solana.nft.listener.service.balance

import com.rarible.blockchain.scanner.framework.data.LogRecordEvent
import com.rarible.blockchain.scanner.publisher.LogRecordEventPublisher
import com.rarible.core.entity.reducer.service.EntityService
import com.rarible.protocol.solana.common.model.Balance
import com.rarible.protocol.solana.common.model.BalanceId
import com.rarible.protocol.solana.common.model.OrderStatus
import com.rarible.protocol.solana.common.model.isEmpty
import com.rarible.protocol.solana.common.records.SolanaAuctionHouseOrderRecord
import com.rarible.protocol.solana.common.records.SolanaOrderUpdateInstruction
import com.rarible.protocol.solana.common.records.SubscriberGroup
import com.rarible.protocol.solana.common.repository.BalanceRepository
import com.rarible.protocol.solana.common.repository.OrderRepository
import com.rarible.protocol.solana.common.update.BalanceUpdateListener
import kotlinx.coroutines.flow.toList
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.math.BigInteger

@Component
class BalanceUpdateService(
    private val balanceRepository: BalanceRepository,
    private val orderRepository: OrderRepository,
    private val balanceUpdateListener: BalanceUpdateListener,
    private val logRecordEventPublisher: LogRecordEventPublisher
) : EntityService<BalanceId, Balance> {

    private val logger = LoggerFactory.getLogger(javaClass)

    override suspend fun get(id: BalanceId): Balance? =
        balanceRepository.findByAccount(id)

    override suspend fun update(entity: Balance): Balance {
        if (entity.isEmpty) {
            logger.info("Balance without Initialize record, skipping it: {}", entity)
            return entity
        }
        val exist = balanceRepository.findByAccount(entity.account)
        val balance = balanceRepository.save(entity)
        if (exist != null) {
            balanceUpdateListener.onBalanceChanged(balance)
        }
        if (exist != null && entity.value == BigInteger.ZERO) {
            logger.info("Deleted balance: $entity")
        } else {
            logger.info("Updated balance: $entity")
        }
        if (exist?.value != balance.value) {
            updateOrders(balance)
        }
        return balance
    }

    private suspend fun updateOrders(balance: Balance) {
        val orders = orderRepository.findSellOrdersByMintAndMaker(
            mint = balance.mint,
            maker = balance.owner,
            statuses = listOf(OrderStatus.ACTIVE, OrderStatus.INACTIVE)
        ).toList()
        if (orders.isEmpty()) {
            return // Just to avoid unnecessary logging
        }

        logger.info("Publishing {} order updates for balance {}", orders.size, balance)
        val events = orders.map { order ->
            val fakeBalanceUpdateRecord = SolanaAuctionHouseOrderRecord.InternalOrderUpdateRecord(
                mint = balance.mint,
                timestamp = balance.updatedAt, // This TS taken from record event
                auctionHouse = order.auctionHouse,
                orderId = order.id,
                instruction = SolanaOrderUpdateInstruction.BalanceUpdate(balance.account)
            )
            LogRecordEvent(fakeBalanceUpdateRecord, false)
        }

        logRecordEventPublisher.publish(
            groupId = SubscriberGroup.AUCTION_HOUSE_ORDER.id,
            logRecordEvents = events
        )

    }
}
