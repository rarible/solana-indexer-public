package com.rarible.protocol.solana.nft.listener.service.escrow

import com.rarible.protocol.solana.common.configuration.SolanaIndexerProperties
import com.rarible.protocol.solana.common.event.EscrowBuyEvent
import com.rarible.protocol.solana.common.event.EscrowDepositEvent
import com.rarible.protocol.solana.common.event.EscrowEvent
import com.rarible.protocol.solana.common.event.EscrowExecuteSaleEvent
import com.rarible.protocol.solana.common.event.EscrowWithdrawEvent
import com.rarible.protocol.solana.common.model.Escrow
import com.rarible.protocol.solana.nft.listener.service.AbstractMetricReducer
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Component

@Component
class EscrowMetricReducer(
    properties: SolanaIndexerProperties,
    meterRegistry: MeterRegistry,
) : AbstractMetricReducer<EscrowEvent, Escrow>(meterRegistry, properties, "escrow") {

    override fun getMetricName(event: EscrowEvent): String {
        return when (event) {
            is EscrowBuyEvent -> "escrow_buy"
            is EscrowDepositEvent -> "escrow_deposit"
            is EscrowExecuteSaleEvent -> "escrow_execute_sale"
            is EscrowWithdrawEvent -> "escrow_withdraw"
        }
    }
}
