package com.rarible.protocol.solana.nft.listener.service.balance

import com.rarible.protocol.solana.nft.listener.configuration.NftIndexerProperties
import com.rarible.protocol.solana.nft.listener.model.Balance
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
