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
    val metaAddress: MetaId,
    val tokenAddress: String,
    val metaData: MetaplexMetaData,
    val updatedAt: Instant,
    val verified: Boolean,
    override val revertableEvents: List<MetaplexMetaEvent>
) : Entity<MetaId, MetaplexMetaEvent, MetaplexMeta> {
    @get:Id
    @get:AccessType(AccessType.Type.PROPERTY)
    override var id: MetaId
        get() = metaAddress
        set(_) {}

    override fun withRevertableEvents(events: List<MetaplexMetaEvent>): MetaplexMeta = copy(revertableEvents = events)

    companion object {
        const val COLLECTION = "metaplex-meta"

        fun empty(metaAddress: MetaId): MetaplexMeta = MetaplexMeta(
            metaAddress = metaAddress,
            tokenAddress = "",
            metaData = MetaplexMetaData(
                name = "",
                symbol = "",
                uri = "",
                sellerFeeBasisPoints = 0,
                creators = emptyList(),
                mutable = false,
                collection = MetaplexMetaData.Collection("", false)
            ),
            updatedAt = Instant.EPOCH,
            verified = false,
            revertableEvents = emptyList()
        )
    }
}
