package com.rarible.protocol.solana.nft.listener.service.balance

import com.rarible.core.entity.reducer.service.EntityService
import com.rarible.protocol.solana.nft.listener.model.Balance
import com.rarible.protocol.solana.nft.listener.model.BalanceId
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class BalanceUpdateService(
    private val balanceRepository: BalanceRepository,
) : EntityService<BalanceId, Balance> {

    override suspend fun get(id: BalanceId): Balance? {
        return balanceRepository.findById(id).awaitFirstOrNull()
    }

    override suspend fun update(entity: Balance): Balance {
        val savedItem = balanceRepository.save(entity).awaitFirst()

        logger.info("Updated item: $entity")
        return savedItem
    }

    companion object {
        private val logger = LoggerFactory.getLogger(BalanceUpdateService::class.java)
    }
}
