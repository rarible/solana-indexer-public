package com.rarible.protocol.solana.nft.listener.service.meta

import com.rarible.core.entity.reducer.service.Reducer
import com.rarible.protocol.solana.common.event.MetaplexCreateMetadataAccountEvent
import com.rarible.protocol.solana.common.event.MetaplexMetaEvent
import com.rarible.protocol.solana.common.event.MetaplexSignMetadataEvent
import com.rarible.protocol.solana.common.event.MetaplexUnVerifyCollectionMetadataEvent
import com.rarible.protocol.solana.common.event.MetaplexUpdateMetadataEvent
import com.rarible.protocol.solana.common.event.MetaplexVerifyCollectionMetadataEvent
import com.rarible.protocol.solana.common.model.MetaplexMeta
import com.rarible.protocol.solana.common.model.MetaplexTokenCreator
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class ReversedMetaplexMetaReducer : Reducer<MetaplexMetaEvent, MetaplexMeta> {

    private val logger = LoggerFactory.getLogger(ReversedMetaplexMetaReducer::class.java)

    override suspend fun reduce(entity: MetaplexMeta, event: MetaplexMetaEvent): MetaplexMeta {
        val revertableEvents = entity.revertableEvents
        if (revertableEvents.isEmpty() || revertableEvents.last().log != event.log) {
            logger.error(
                "Revertable event error: attempt to revert an event which is not the latest one, event: $event, entity: $entity"
            )
            return entity
        }
        val beforeRevertedEvents = revertableEvents.dropLast(1)
        return when (event) {
            is MetaplexCreateMetadataAccountEvent -> MetaplexMeta.empty(event.metaAddress)
            is MetaplexVerifyCollectionMetadataEvent -> {
                val wasVerifiedBefore = beforeRevertedEvents.lastOrNull {
                    it is MetaplexVerifyCollectionMetadataEvent || it is MetaplexUnVerifyCollectionMetadataEvent
                }?.let { it is MetaplexVerifyCollectionMetadataEvent }
                entity.copy(
                    metaFields = entity.metaFields.copy(
                        collection = entity.metaFields.collection?.copy(
                            verified = wasVerifiedBefore ?: true
                        )
                    )
                )
            }
            is MetaplexUnVerifyCollectionMetadataEvent -> {
                val wasVerifiedBefore = beforeRevertedEvents.lastOrNull {
                    it is MetaplexVerifyCollectionMetadataEvent || it is MetaplexUnVerifyCollectionMetadataEvent
                }?.let { it is MetaplexVerifyCollectionMetadataEvent }
                entity.copy(
                    metaFields = entity.metaFields.copy(
                        collection = entity.metaFields.collection?.copy(
                            verified = wasVerifiedBefore ?: false
                        )
                    )
                )
            }
            is MetaplexUpdateMetadataEvent -> {
                val lastMetaFields = beforeRevertedEvents.reversed().asSequence().mapNotNull {
                    when (it) {
                        is MetaplexCreateMetadataAccountEvent -> it.metadata
                        is MetaplexUpdateMetadataEvent -> it.newMetadata
                        else -> null
                    }
                }.firstOrNull() ?: MetaplexMeta.emptyMetaFields
                entity.copy(
                    metaFields = lastMetaFields
                )
            }
            is MetaplexSignMetadataEvent -> entity.copy(
                metaFields = entity.metaFields.copy(
                    creators = entity.metaFields.creators?.map {
                        if (it.address == event.creatorAddress) {
                            MetaplexTokenCreator(it.address, it.share, verified = false)
                        } else {
                            it
                        }
                    }
                )
            )
        }.copy(updatedAt = event.timestamp)
    }
}
