package com.rarible.protocol.solana.nft.listener.service.meta

import com.rarible.core.entity.reducer.service.EntityIdService
import com.rarible.protocol.solana.common.event.MetaplexMetaEvent
import com.rarible.protocol.solana.common.model.MetaId
import org.springframework.stereotype.Component

@Component
class MetaIdService : EntityIdService<MetaplexMetaEvent, MetaId> {
    override fun getEntityId(event: MetaplexMetaEvent): MetaId {
        return event.metaAddress
    }
}