package com.rarible.protocol.solana.nft.listener.service.meta

import com.rarible.protocol.solana.common.event.MetaplexCreateMetadataAccountEvent
import com.rarible.protocol.solana.common.event.MetaplexMetaEvent
import com.rarible.protocol.solana.common.event.MetaplexSetAndVerifyCollectionEvent
import com.rarible.protocol.solana.common.event.MetaplexUnVerifyCollectionMetadataEvent
import com.rarible.protocol.solana.common.event.MetaplexUpdateMetadataEvent
import com.rarible.protocol.solana.common.event.MetaplexVerifyCollectionMetadataEvent
import com.rarible.protocol.solana.common.event.MetaplexVerifyCreatorEvent
import com.rarible.protocol.solana.common.records.SolanaMetaRecord
import org.springframework.stereotype.Component

@Component
class MetaEventConverter {
    suspend fun convert(
        record: SolanaMetaRecord,
        reversed: Boolean
    ): List<MetaplexMetaEvent> = when (record) {
        is SolanaMetaRecord.MetaplexCreateMetadataAccountRecord -> listOf(
            MetaplexCreateMetadataAccountEvent(
                metaAddress = record.metaAccount,
                log = record.log,
                reversed = reversed,
                token = record.mint,
                timestamp = record.timestamp,
                metadata = record.meta,
                isMutable = record.mutable
            )
        )
        is SolanaMetaRecord.MetaplexUpdateMetadataRecord -> listOf(
            MetaplexUpdateMetadataEvent(
                metaAddress = record.metaAccount,
                log = record.log,
                reversed = reversed,
                timestamp = record.timestamp,
                newMetadata = record.updatedMeta,
                newIsMutable = record.updatedMutable
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
        is SolanaMetaRecord.MetaplexUnVerifyCollectionRecord -> listOf(
            MetaplexUnVerifyCollectionMetadataEvent(
                metaAddress = record.metaAccount,
                log = record.log,
                reversed = reversed,
                timestamp = record.timestamp
            )
        )
        is SolanaMetaRecord.MetaplexSignMetadataRecord -> listOf(
            MetaplexVerifyCreatorEvent(
                creatorAddress = record.creatorAddress,
                metaAddress = record.metaAccount,
                log = record.log,
                reversed = reversed,
                timestamp = record.timestamp
            )
        )
        is SolanaMetaRecord.SetAndVerifyMetadataRecord -> listOf(
            MetaplexSetAndVerifyCollectionEvent(
                mint = record.mint,
                collection = record.metaAccount,
                metaAddress = record.metaAccount,
                log = record.log,
                reversed = reversed,
                timestamp = record.timestamp
            )
        )
    }
}
