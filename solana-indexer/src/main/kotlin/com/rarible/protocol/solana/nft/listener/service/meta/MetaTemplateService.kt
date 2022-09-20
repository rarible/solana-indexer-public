package com.rarible.protocol.solana.nft.listener.service.meta

import com.rarible.core.entity.reducer.service.EntityTemplateProvider
import com.rarible.protocol.solana.common.model.MetaId
import com.rarible.protocol.solana.common.model.MetaplexMeta
import org.springframework.stereotype.Component

@Component
class MetaTemplateProvider : EntityTemplateProvider<MetaId, MetaplexMeta> {
    override fun getEntityTemplate(id: MetaId, version: Long?): MetaplexMeta = MetaplexMeta.empty(id, version)
}