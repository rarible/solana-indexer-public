package com.rarible.protocol.solana.nft.listener.service.meta

import com.rarible.core.entity.reducer.service.EntityService
import com.rarible.protocol.solana.common.model.MetaId
import com.rarible.protocol.solana.common.model.MetaplexMeta
import com.rarible.protocol.solana.common.repository.MetaRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class MetaUpdateService(
    private val metaRepository: MetaRepository
) : EntityService<MetaId, MetaplexMeta> {

    override suspend fun get(id: MetaId): MetaplexMeta? =
        metaRepository.findById(id)

    override suspend fun update(entity: MetaplexMeta): MetaplexMeta {
        val balance = metaRepository.save(entity)
        logger.info("Updated balance: $entity")
        return balance
    }

    companion object {
        private val logger = LoggerFactory.getLogger(MetaUpdateService::class.java)
    }
}