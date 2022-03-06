package com.rarible.protocol.solana.nft.listener.service.meta

import com.rarible.protocol.solana.common.configuration.SolanaIndexerProperties
import com.rarible.protocol.solana.common.event.MetaplexCreateMetadataAccountEvent
import com.rarible.protocol.solana.common.event.MetaplexMetaEvent
import com.rarible.protocol.solana.common.event.MetaplexSetAndVerifyCollectionEvent
import com.rarible.protocol.solana.common.event.MetaplexVerifyCreatorEvent
import com.rarible.protocol.solana.common.event.MetaplexUnVerifyCollectionMetadataEvent
import com.rarible.protocol.solana.common.event.MetaplexUpdateMetadataEvent
import com.rarible.protocol.solana.common.event.MetaplexVerifyCollectionMetadataEvent
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
            is MetaplexCreateMetadataAccountEvent -> "create_metadata_account"
            is MetaplexVerifyCollectionMetadataEvent -> "verify_collection"
            is MetaplexUnVerifyCollectionMetadataEvent -> "un_verify_collection"
            is MetaplexUpdateMetadataEvent -> "update"
            is MetaplexVerifyCreatorEvent -> "sign"
            is MetaplexSetAndVerifyCollectionEvent -> "set_and_verify"
        }
    }
}
