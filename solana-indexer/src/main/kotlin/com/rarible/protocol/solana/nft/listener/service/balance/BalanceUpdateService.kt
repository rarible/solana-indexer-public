package com.rarible.protocol.solana.nft.listener.service.balance

import com.rarible.core.entity.reducer.service.EntityService
import com.rarible.protocol.solana.common.model.Balance
import com.rarible.protocol.solana.common.model.BalanceId
import com.rarible.protocol.solana.common.model.isEmpty
import com.rarible.protocol.solana.common.repository.BalanceRepository
import com.rarible.protocol.solana.common.update.BalanceUpdateListener
import com.rarible.protocol.solana.nft.listener.service.order.OrderMakeStockBalanceUpdateService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class BalanceUpdateService(
    private val balanceRepository: BalanceRepository,
    private val balanceUpdateListener: BalanceUpdateListener,
    private val orderMakeStockBalanceUpdateService: OrderMakeStockBalanceUpdateService
) : EntityService<BalanceId, Balance> {

    private val logger = LoggerFactory.getLogger(javaClass)

    override suspend fun get(id: BalanceId): Balance? =
        balanceRepository.findByAccount(id)

    override suspend fun update(entity: Balance): Balance {
        if (entity.isEmpty) {
            logger.info("Balance without Initialize record, skipping it: {}", entity)
            return entity
        }
        val balance = balanceRepository.save(entity)
        balanceUpdateListener.onBalanceChanged(balance)
        orderMakeStockBalanceUpdateService.updateMakeStockOfSellOrders(balance)
        logger.info("Updated balance: $balance")
        return balance
    }

}
