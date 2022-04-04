package com.rarible.protocol.solana.nft.listener.service.meta

import com.rarible.core.entity.reducer.service.Reducer
import com.rarible.protocol.solana.common.event.MetaplexCreateMetadataAccountEvent
import com.rarible.protocol.solana.common.event.MetaplexMetaEvent
import com.rarible.protocol.solana.common.event.MetaplexSetAndVerifyCollectionEvent
import com.rarible.protocol.solana.common.event.MetaplexVerifyCreatorEvent
import com.rarible.protocol.solana.common.event.MetaplexUnVerifyCollectionMetadataEvent
import com.rarible.protocol.solana.common.event.MetaplexUpdateMetadataEvent
import com.rarible.protocol.solana.common.event.MetaplexVerifyCollectionMetadataEvent
import com.rarible.protocol.solana.common.model.MetaplexMeta
import com.rarible.protocol.solana.common.model.MetaplexTokenCreator
import com.rarible.protocol.solana.common.model.isEmpty
import org.springframework.stereotype.Component

@Component
class ForwardMetaplexMetaReducer : Reducer<MetaplexMetaEvent, MetaplexMeta> {
    override suspend fun reduce(entity: MetaplexMeta, event: MetaplexMetaEvent): MetaplexMeta {
        if (event !is MetaplexCreateMetadataAccountEvent && entity.isEmpty) {
            return entity
        }
        return when (event) {
            is MetaplexCreateMetadataAccountEvent -> entity.copy(
                createdAt = event.timestamp,
                tokenAddress = event.token,
                metaFields = event.metadata,
                isMutable = event.isMutable
            )
            // TODO[test]: add a test for updating metadata.
            is MetaplexUpdateMetadataEvent -> entity.copy(
                metaFields = event.newMetadata ?: entity.metaFields,
                isMutable = event.newIsMutable ?: entity.isMutable
            )
            is MetaplexVerifyCollectionMetadataEvent -> entity.copy(
                metaFields = entity.metaFields.copy(
                    collection = entity.metaFields.collection?.copy(
                        verified = true
                    )
                )
            )
            // TODO[test]: add a test for UnVerify collection.
            is MetaplexUnVerifyCollectionMetadataEvent -> entity.copy(
                metaFields = entity.metaFields.copy(
                    collection = entity.metaFields.collection?.copy(
                        verified = false
                    )
                )
            )
            is MetaplexVerifyCreatorEvent -> entity.copy(
                metaFields = entity.metaFields.copy(
                    creators = entity.metaFields.creators?.map {
                        if (it.address == event.creatorAddress) {
                            MetaplexTokenCreator(it.address, it.share, verified = true)
                        } else {
                            it
                        }
                    }
                )
            )
            // TODO[test]: add a test for set and verify metadata.
            is MetaplexSetAndVerifyCollectionEvent -> entity.copy(
                tokenAddress = event.mint,
                metaFields = entity.metaFields.copy(
                    collection = entity.metaFields.collection?.copy(
                        verified = true
                    )
                )
            )
        }.copy(updatedAt = event.timestamp)
    }
}
