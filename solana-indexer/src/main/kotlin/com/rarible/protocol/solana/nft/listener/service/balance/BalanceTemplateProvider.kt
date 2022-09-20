package com.rarible.protocol.solana.nft.listener.service.balance

import com.rarible.core.entity.reducer.service.EntityTemplateProvider
import com.rarible.protocol.solana.common.model.Balance
import com.rarible.protocol.solana.common.model.BalanceId
import org.springframework.stereotype.Component

@Component
class BalanceTemplateProvider : EntityTemplateProvider<BalanceId, Balance> {
    override fun getEntityTemplate(id: BalanceId, version: Long?): Balance = Balance.empty(id, version)
}
