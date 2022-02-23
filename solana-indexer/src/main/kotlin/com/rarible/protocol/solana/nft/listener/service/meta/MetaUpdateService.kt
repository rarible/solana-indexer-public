package com.rarible.protocol.solana.nft.listener.service.meta

import com.rarible.core.entity.reducer.service.EntityService
import com.rarible.protocol.solana.common.meta.TokenMetaService
import com.rarible.protocol.solana.common.model.MetaId
import com.rarible.protocol.solana.common.model.MetaplexMeta
import com.rarible.protocol.solana.common.repository.MetaplexMetaRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class MetaUpdateService(
    private val metaplexMetaRepository: MetaplexMetaRepository,
    private val tokenMetaService: TokenMetaService
) : EntityService<MetaId, MetaplexMeta> {

    override suspend fun get(id: MetaId): MetaplexMeta? =
        metaplexMetaRepository.findByMetaAddress(id)

    override suspend fun update(entity: MetaplexMeta): MetaplexMeta {
        val meta = metaplexMetaRepository.save(entity)
        logger.info("Updated metaplex meta: $entity")
        tokenMetaService.onMetaplexMetaChanged(meta)
        return meta
    }

    companion object {
        private val logger = LoggerFactory.getLogger(MetaUpdateService::class.java)
    }
}
