package com.rarible.protocol.solana.nft.listener.service.item

import com.rarible.blockchain.scanner.framework.data.LogRecordEvent
import com.rarible.protocol.solana.nft.listener.configuration.NftIndexerProperties
import com.rarible.protocol.solana.nft.listener.model.Item
import com.rarible.protocol.solana.nft.listener.model.ItemEvent
import com.rarible.protocol.solana.nft.listener.service.records.SolanaItemLogRecord.BurnRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaItemLogRecord.CreateMetadataRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaItemLogRecord.InitializeAccountRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaItemLogRecord.InitializeMintRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaItemLogRecord.MintToRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaItemLogRecord.TransferRecord
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Component

@Component
class ItemMetricReducer(
    properties: NftIndexerProperties,
    meterRegistry: MeterRegistry,
) : AbstractMetricReducer<LogRecordEvent<ItemEvent>, Item>(meterRegistry, properties, "item") {

    override fun getMetricName(event: LogRecordEvent<ItemEvent>): String {
        return when (event.record) {
            is BurnRecord -> "burn"
            is MintToRecord -> "mint"
            is TransferRecord -> "transfer"
            is CreateMetadataRecord -> "creators"
            is InitializeAccountRecord -> "initialize-account"
            is InitializeMintRecord -> "initialize-mint"
        }
    }
}