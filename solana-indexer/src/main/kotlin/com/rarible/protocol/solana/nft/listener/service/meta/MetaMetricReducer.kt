package com.rarible.protocol.solana.nft.listener.service.meta

import com.rarible.protocol.solana.common.configuration.SolanaIndexerProperties
import com.rarible.protocol.solana.common.event.MetaplexCreateMetadataEvent
import com.rarible.protocol.solana.common.event.MetaplexMetaEvent
import com.rarible.protocol.solana.common.event.MetaplexVerifyMetadataEvent
import com.rarible.protocol.solana.common.model.MetaplexMeta
import com.rarible.protocol.solana.nft.listener.service.AbstractMetricReducer
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Component

@Component
class MetaplexMetaMetricReducer(
    properties: SolanaIndexerProperties,
    meterRegistry: MeterRegistry,
) : AbstractMetricReducer<MetaplexMetaEvent, MetaplexMeta>(meterRegistry, properties, "meta") {

    override fun getMetricName(event: MetaplexMetaEvent): String {
        return when (event) {
            is MetaplexCreateMetadataEvent -> "create-meta"
            is MetaplexVerifyMetadataEvent -> "verify-meta"
        }
    }
}