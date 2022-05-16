package com.rarible.protocol.solana.nft.listener.service.escrow

import com.rarible.core.entity.reducer.service.EntityService
import com.rarible.protocol.solana.common.model.Escrow
import com.rarible.protocol.solana.common.model.EscrowId
import com.rarible.protocol.solana.common.model.isEmpty
import com.rarible.protocol.solana.common.repository.EscrowRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class EscrowUpdateService(
    private val escrowRepository: EscrowRepository
) : EntityService<EscrowId, Escrow> {
    override suspend fun get(id: EscrowId): Escrow? {
        return escrowRepository.findByAccount(id)
    }

    override suspend fun update(entity: Escrow): Escrow {
        if (entity.isEmpty) {
            logger.info("Escrow is empty, skipping it: {}", entity.account)
            return entity
        }
        val escrow = escrowRepository.save(entity)

        logger.info("Updated escrow: $entity")
        return escrow
    }

    companion object {
        private val logger = LoggerFactory.getLogger(EscrowUpdateService::class.java)
    }
}