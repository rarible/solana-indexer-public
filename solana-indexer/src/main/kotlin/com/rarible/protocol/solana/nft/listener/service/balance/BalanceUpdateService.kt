package com.rarible.protocol.solana.nft.listener.service.balance

import com.rarible.core.entity.reducer.service.EntityService
import com.rarible.protocol.solana.common.model.Balance
import com.rarible.protocol.solana.common.model.BalanceId
import com.rarible.protocol.solana.common.repository.BalanceRepository
import com.rarible.protocol.solana.common.update.BalanceUpdateListener
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class BalanceUpdateService(
    private val balanceRepository: BalanceRepository,
    private val balanceUpdateListener: BalanceUpdateListener
) : EntityService<BalanceId, Balance> {

    override suspend fun get(id: BalanceId): Balance? =
        balanceRepository.findByAccount(id)

    override suspend fun update(entity: Balance): Balance {
        val balance = balanceRepository.save(entity)
        balanceUpdateListener.onBalanceChanged(balance)
        logger.info("Updated balance: $entity")
        return balance
    }

    companion object {
        private val logger = LoggerFactory.getLogger(BalanceUpdateService::class.java)
    }
}
