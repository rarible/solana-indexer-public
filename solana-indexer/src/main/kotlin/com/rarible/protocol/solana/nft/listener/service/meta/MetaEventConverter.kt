package com.rarible.protocol.solana.nft.listener.service.meta

import com.rarible.protocol.solana.borsh.MetaplexMetadataProgram
import com.rarible.protocol.solana.common.event.MetaplexMetaEvent
import com.rarible.protocol.solana.common.event.MetaplexCreateMetadataEvent
import com.rarible.protocol.solana.common.model.MetaplexTokenCreator
import com.rarible.protocol.solana.common.model.MetaplexTokenMeta
import com.rarible.protocol.solana.nft.listener.consumer.SolanaLogRecordEvent
import com.rarible.protocol.solana.nft.listener.service.records.SolanaBaseLogRecord.MetaplexCreateMetadataRecord
import org.springframework.stereotype.Component

@Component
class MetaEventConverter {
    suspend fun convert(source: SolanaLogRecordEvent): List<MetaplexMetaEvent> {
        val timestamp = source.record.timestamp

        return when (val record = source.record) {
            is MetaplexCreateMetadataRecord -> {
                val rawData = record.data
                listOf(
                    MetaplexCreateMetadataEvent(
                        metaAddress = record.account,
                        log = record.log,
                        reversed = source.reversed,
                        token = record.mint,
                        timestamp = timestamp,
                        metadata = rawData.convert()
                    )
                )
            }
            else -> emptyList()
        }
    }

    private fun MetaplexMetadataProgram.Data.convert() = MetaplexTokenMeta(
        name = name,
        symbol = symbol,
        uri = uri,
        sellerFeeBasisPoints = sellerFeeBasisPoints.toInt(),
        creators = creators.orEmpty().map {
            MetaplexTokenCreator(
                address = it.address,
                share = it.share.toInt()
            )
        },
        collection = collection?.let {
            MetaplexTokenMeta.Collection(
                address = it.key,
                verified = it.verified
            )
        },
        mutable = mutable
    )

}
