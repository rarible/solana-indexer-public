package com.rarible.protocol.solana.common.model

import com.rarible.core.entity.reducer.model.Entity
import com.rarible.protocol.solana.common.event.MetaplexMetaEvent
import org.springframework.data.annotation.AccessType
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

typealias MetaId = String

@Document("metaplex-meta")
data class MetaplexMeta(
    val tokenAddress: String,
    val metaAddress: MetaId,
    val meta: MetaplexTokenMeta,
    val updatedAt: Instant,
    override val revertableEvents: List<MetaplexMetaEvent>
) : Entity<MetaId, MetaplexMetaEvent, MetaplexMeta> {
    @get:Id
    @get:AccessType(AccessType.Type.PROPERTY)
    override var id: MetaId
        get() = metaAddress
        set(_) {}

    override fun withRevertableEvents(events: List<MetaplexMetaEvent>): MetaplexMeta = copy(revertableEvents = events)

    companion object {
        fun empty(metaAddress: MetaId): MetaplexMeta = MetaplexMeta(
            "",
            metaAddress,
            MetaplexTokenMeta("", "", "", 0, emptyList(), false, MetaplexTokenMeta.Collection("", false)),
            Instant.EPOCH,
            emptyList()
        )
    }
}