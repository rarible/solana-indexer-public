package com.rarible.protocol.solana.nft.listener.service.escrow

import com.rarible.core.entity.reducer.service.EntityTemplateProvider
import com.rarible.protocol.solana.common.model.Escrow
import com.rarible.protocol.solana.common.model.EscrowId
import org.springframework.stereotype.Component

@Component
class EscrowTemplateProvider : EntityTemplateProvider<EscrowId, Escrow> {
    override fun getEntityTemplate(id: EscrowId, version: Long?): Escrow = Escrow.empty()
}
