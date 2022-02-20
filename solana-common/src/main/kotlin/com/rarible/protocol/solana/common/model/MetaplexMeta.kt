package com.rarible.protocol.solana.common.model

import com.rarible.core.entity.reducer.model.Entity
import com.rarible.protocol.solana.common.event.MetaplexMetaEvent
import org.springframework.data.annotation.AccessType
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

typealias MetaId = String

@Document(MetaplexMeta.COLLECTION)
data class MetaplexMeta(
    @Id
    val metaAddress: MetaId,
    val tokenAddress: String,
    val metaFields: MetaplexMetaFields,
    val isMutable: Boolean,
    val updatedAt: Instant,
    override val revertableEvents: List<MetaplexMetaEvent>
) : Entity<MetaId, MetaplexMetaEvent, MetaplexMeta> {

    override val id: MetaId get() = metaAddress

    override fun withRevertableEvents(events: List<MetaplexMetaEvent>): MetaplexMeta =
        copy(revertableEvents = events)

    companion object {
        const val COLLECTION = "metaplex-meta"

        fun empty(metaAddress: MetaId): MetaplexMeta = MetaplexMeta(
            metaAddress = metaAddress,
            tokenAddress = "",
            metaFields = MetaplexMetaFields(
                name = "",
                symbol = "",
                uri = "",
                sellerFeeBasisPoints = 0,
                creators = emptyList(),
                collection = MetaplexMetaFields.Collection("", false)
            ),
            updatedAt = Instant.EPOCH,
            revertableEvents = emptyList(),
            isMutable = false
        )
    }
}
