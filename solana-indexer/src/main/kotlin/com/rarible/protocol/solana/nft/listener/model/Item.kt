package com.rarible.protocol.solana.nft.listener.model

import com.rarible.core.entity.reducer.model.Entity
import com.rarible.protocol.solana.nft.listener.consumer.SolanaLogRecordEvent
import com.rarible.protocol.solana.nft.listener.service.records.SolanaItemLogRecord
import org.springframework.data.annotation.AccessType
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

typealias ItemEvent = SolanaItemLogRecord
typealias ItemId = String

@Document("item")
data class Item(
    val token: String,
    val collection: String? = null,
    val supply: Long,
    override val revertableEvents: List<SolanaLogRecordEvent> = emptyList(),
) : Entity<ItemId, SolanaLogRecordEvent, Item> {
    @get:Id
    @get:AccessType(AccessType.Type.PROPERTY)
    override var id: ItemId
        get() = token
        set(_) {}

    override fun withRevertableEvents(events: List<SolanaLogRecordEvent>): Item {
        return copy(revertableEvents = events)
    }

    companion object {
        fun empty(token: String): Item {
            return Item(
                token = token,
                collection = null,
                supply = 0L,
                revertableEvents = emptyList()
            )
        }
    }
}
