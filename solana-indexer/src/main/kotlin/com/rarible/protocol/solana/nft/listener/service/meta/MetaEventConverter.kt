package com.rarible.protocol.solana.nft.listener.service.meta

import com.rarible.protocol.solana.borsh.MetaplexMetadataProgram
import com.rarible.protocol.solana.common.event.MetaplexCreateMetadataEvent
import com.rarible.protocol.solana.common.event.MetaplexMetaEvent
import com.rarible.protocol.solana.common.event.MetaplexUpdateMetadataEvent
import com.rarible.protocol.solana.common.event.MetaplexVerifyCollectionMetadataEvent
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
        is SolanaMetaRecord.MetaplexCreateMetadataRecord -> listOf(
            MetaplexCreateMetadataEvent(
                metaAddress = record.metaAccount,
                log = record.log,
                reversed = reversed,
                token = record.mint,
                timestamp = record.timestamp,
                metadata = record.data.metadata.convert(),
                isMutable = record.data.mutable
            )
        )
        is SolanaMetaRecord.MetaplexUpdateMetadataRecord -> listOf(
            MetaplexUpdateMetadataEvent(
                metaAddress = record.metaAccount,
                log = record.log,
                reversed = reversed,
                timestamp = record.timestamp,
                newMetadata = record.updateArgs.metadata?.convert(),
                newIsMutable = record.updateArgs.mutable
            )
        )
        is SolanaMetaRecord.MetaplexVerifyCollectionRecord -> listOf(
            MetaplexVerifyCollectionMetadataEvent(
                metaAddress = record.metaAccount,
                log = record.log,
                reversed = reversed,
                timestamp = record.timestamp
            )
        )
    }

    private fun MetaplexMetadataProgram.Data.convert() = MetaplexMetaFields(
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
        }
    )
}
