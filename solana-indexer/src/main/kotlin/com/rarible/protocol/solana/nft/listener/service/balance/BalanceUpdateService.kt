package com.rarible.protocol.solana.nft.listener.service.balance

import com.rarible.core.entity.reducer.service.EntityService
import com.rarible.protocol.solana.common.filter.token.SolanaTokenFilter
import com.rarible.protocol.solana.common.model.Balance
import com.rarible.protocol.solana.common.model.BalanceId
import com.rarible.protocol.solana.common.model.isEmpty
import com.rarible.protocol.solana.common.repository.BalanceRepository
import com.rarible.protocol.solana.common.update.BalanceUpdateListener
import com.rarible.protocol.solana.nft.listener.service.order.OrderMakeStockBalanceUpdateService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.math.BigInteger

@Component
class BalanceUpdateService(
    private val balanceRepository: BalanceRepository,
    private val balanceUpdateListener: BalanceUpdateListener,
    private val orderMakeStockBalanceUpdateService: OrderMakeStockBalanceUpdateService,
    private val tokenFilter: SolanaTokenFilter
) : EntityService<BalanceId, Balance> {

    private val logger = LoggerFactory.getLogger(javaClass)

    override suspend fun get(id: BalanceId): Balance? =
        balanceRepository.findByAccount(id)

    override suspend fun update(entity: Balance): Balance {
        if (entity.isEmpty) {
            logger.info("Balance without Initialize record, skipping it: {}", entity)
            return entity
        }
        if (!tokenFilter.isAcceptableToken(entity.mint)) {
            logger.info("Balance update is ignored because mint ${entity.mint} is filtered out")
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
            orderMakeStockBalanceUpdateService.updateMakeStockOfSellOrders(balance)
        }
        return balance
    }

}
