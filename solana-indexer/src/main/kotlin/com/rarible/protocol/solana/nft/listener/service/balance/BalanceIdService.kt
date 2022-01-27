package com.rarible.protocol.solana.nft.listener.service.balance

import com.rarible.core.entity.reducer.service.EntityIdService
import com.rarible.protocol.solana.common.event.BalanceEvent
import com.rarible.protocol.solana.common.model.BalanceId
import org.springframework.stereotype.Component

@Component
class BalanceIdService : EntityIdService<BalanceEvent, BalanceId> {
    override fun getEntityId(event: BalanceEvent): BalanceId {
        return event.account
    }
}
