package com.rarible.protocol.solana.nft.listener.service.token

import com.rarible.protocol.solana.nft.listener.configuration.NftIndexerProperties
import com.rarible.protocol.solana.nft.listener.model.Token
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Component

@Component
class TokenMetricReducer(
    properties: NftIndexerProperties,
    meterRegistry: MeterRegistry,
) : AbstractMetricReducer<TokenEvent, Token>(meterRegistry, properties, "item") {

    override fun getMetricName(event: TokenEvent): String {
        return when (event) {
            is BurnEvent -> "burn"
            is MintEvent -> "mint"
            is TransferEvent -> "transfer"
        }
    }
}