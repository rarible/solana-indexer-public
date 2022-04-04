package com.rarible.protocol.solana.common.model

import com.rarible.protocol.solana.common.event.MetaplexMetaEvent
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
    override val createdAt: Instant?,
    override val updatedAt: Instant,
    override val revertableEvents: List<MetaplexMetaEvent>
) : SolanaEntity<MetaId, MetaplexMetaEvent, MetaplexMeta> {

    override val id: MetaId get() = metaAddress

    override fun withRevertableEvents(events: List<MetaplexMetaEvent>): MetaplexMeta =
        copy(revertableEvents = events)

    override fun toString(): String =
        "MetaplexMeta(metaAddress='$metaAddress', tokenAddress='$tokenAddress', metaFields=$metaFields, " +
                "isMutable=$isMutable, createdAt=$createdAt, updatedAt=$updatedAt)"

    companion object {
        const val COLLECTION = "metaplex-meta"

        fun empty(metaAddress: MetaId): MetaplexMeta = MetaplexMeta(
            metaAddress = metaAddress,
            tokenAddress = "",
            metaFields = emptyMetaFields,
            createdAt = Instant.EPOCH,
            updatedAt = Instant.EPOCH,
            revertableEvents = emptyList(),
            isMutable = false
        )

        val emptyMetaFields: MetaplexMetaFields = MetaplexMetaFields(
            name = "",
            symbol = "",
            uri = "",
            sellerFeeBasisPoints = 0,
            creators = emptyList(),
            collection = MetaplexMetaFields.Collection("", false)
        )
    }
}
