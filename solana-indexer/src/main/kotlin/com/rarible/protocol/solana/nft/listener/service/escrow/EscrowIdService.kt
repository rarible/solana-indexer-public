package com.rarible.protocol.solana.nft.listener.service.escrow

import com.rarible.core.entity.reducer.service.EntityIdService
import com.rarible.protocol.solana.common.event.EscrowEvent
import com.rarible.protocol.solana.common.model.EscrowId
import org.springframework.stereotype.Component

@Component
class EscrowIdService : EntityIdService<EscrowEvent, EscrowId> {
    override fun getEntityId(event: EscrowEvent): EscrowId {
        return event.account
    }
}
