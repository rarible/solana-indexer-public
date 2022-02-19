package com.rarible.protocol.solana.nft.listener.service.meta

import com.rarible.protocol.solana.borsh.MetaplexMetadataProgram
import com.rarible.protocol.solana.common.event.MetaplexCreateMetadataEvent
import com.rarible.protocol.solana.common.event.MetaplexMetaEvent
import com.rarible.protocol.solana.common.event.MetaplexVerifyMetadataEvent
import com.rarible.protocol.solana.common.model.MetaplexMetaFields
import com.rarible.protocol.solana.common.model.MetaplexTokenCreator
import com.rarible.protocol.solana.nft.listener.service.records.SolanaMetaRecord
import org.springframework.stereotype.Component

@Component
class MetaEventConverter {
    suspend fun convert(
        record: SolanaMetaRecord,
        reversed: Boolean
    ): List<MetaplexMetaEvent> = when (record) {
        is SolanaMetaRecord.MetaplexCreateMetadataRecord -> {
            val rawData = record.data.metadata
            listOf(
                MetaplexCreateMetadataEvent(
                    metaAddress = record.metaAccount,
                    log = record.log,
                    reversed = reversed,
                    token = record.mint,
                    timestamp = record.timestamp,
                    metadata = rawData.convert(record.data.mutable)
                )
            )
        }
        is SolanaMetaRecord.MetaplexVerifyCollectionRecord -> listOf(
            MetaplexVerifyMetadataEvent(
                metaAddress = record.metaAccount,
                log = record.log,
                reversed = reversed,
                timestamp = record.timestamp
            )
        )
        else -> emptyList()
    }

    private fun MetaplexMetadataProgram.Data.convert(mutable: Boolean) = MetaplexMetaFields(
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
            MetaplexMetaFields.Collection(
                address = it.key,
                verified = it.verified
            )
        },
        mutable = mutable
    )
}
