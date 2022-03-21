package com.rarible.protocol.solana.nft.listener.service.order

import com.rarible.protocol.solana.common.configuration.SolanaIndexerProperties
import com.rarible.protocol.solana.common.event.ExecuteSaleEvent
import com.rarible.protocol.solana.common.event.OrderBuyEvent
import com.rarible.protocol.solana.common.event.OrderEvent
import com.rarible.protocol.solana.common.event.OrderSellEvent
import com.rarible.protocol.solana.common.model.Order
import com.rarible.protocol.solana.nft.listener.service.AbstractMetricReducer
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Component

@Component
class OrderMetricReducer(
    properties: SolanaIndexerProperties,
    meterRegistry: MeterRegistry,
) : AbstractMetricReducer<OrderEvent, Order>(meterRegistry, properties, "order") {

    override fun getMetricName(event: OrderEvent): String {
        return when (event) {
            is ExecuteSaleEvent -> "execute_sale"
            is OrderBuyEvent -> "buy"
            is OrderSellEvent -> "sell"
        }
    }
}
