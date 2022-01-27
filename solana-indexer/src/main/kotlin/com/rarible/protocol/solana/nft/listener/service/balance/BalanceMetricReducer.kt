package com.rarible.protocol.solana.nft.listener.service.balance

import com.rarible.protocol.solana.common.event.BalanceEvent
import com.rarible.protocol.solana.common.event.BalanceIncomeEvent
import com.rarible.protocol.solana.common.event.BalanceOutcomeEvent
import com.rarible.protocol.solana.nft.listener.configuration.NftIndexerProperties
import com.rarible.protocol.solana.common.model.Balance
import com.rarible.protocol.solana.nft.listener.service.AbstractMetricReducer
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Component

@Component
class BalanceMetricReducer(
    properties: NftIndexerProperties,
    meterRegistry: MeterRegistry,
) : AbstractMetricReducer<BalanceEvent, Balance>(meterRegistry, properties, "balance") {

    override fun getMetricName(event: BalanceEvent): String {
        return when (event) {
            is BalanceOutcomeEvent -> "transfer_from"
            is BalanceIncomeEvent -> "transfer_to"
        }
    }
}
