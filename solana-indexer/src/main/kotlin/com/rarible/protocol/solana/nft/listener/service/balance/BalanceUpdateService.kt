package com.rarible.protocol.solana.nft.listener.service.balance

import com.rarible.core.entity.reducer.service.EntityService
import com.rarible.protocol.solana.common.model.Balance
import com.rarible.protocol.solana.common.model.BalanceId
import com.rarible.protocol.solana.common.repository.BalanceRepository
import com.rarible.protocol.solana.common.update.BalanceUpdateListener
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class BalanceUpdateService(
    private val balanceRepository: BalanceRepository,
    private val balanceUpdateListener: BalanceUpdateListener
) : EntityService<BalanceId, Balance> {

    override suspend fun get(id: BalanceId): Balance? =
        balanceRepository.findByAccount(id)

    override suspend fun update(entity: Balance): Balance {
        if (entity.createdAt == Instant.EPOCH) {
            // Field 'createdAt' has real value only if Initialize event received for the Balance
            // if we don't have such init event, balance can't be calculated in right way, so we skip it
            logger.info("Balance without Initialize record, skipping it: {}", entity.account)
            return entity
        }
        val balance = balanceRepository.save(entity)
        balanceUpdateListener.onBalanceChanged(balance)
        logger.info("Updated balance: $entity")
        return balance
    }

    companion object {
        private val logger = LoggerFactory.getLogger(BalanceUpdateService::class.java)
    }
}
