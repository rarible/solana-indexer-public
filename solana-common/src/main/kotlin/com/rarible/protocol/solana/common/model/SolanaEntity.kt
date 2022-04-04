package com.rarible.protocol.solana.common.model

import com.rarible.core.entity.reducer.model.Entity
import java.time.Instant

interface SolanaEntity<Id, Event, E : SolanaEntity<Id, Event, E>> : Entity<Id, Event, E> {
    val createdAt: Instant? // TODO: make not null when MetaplexMeta is migrated.
    val updatedAt: Instant
}

/**
 * Returns `true` only if this entity is not the empty template used during reducing.
 *
 * For example, if we are indexing not from the very beginning,
 * some transfer events may try to reduce non-existing balance (of which we haven't seen the InitializeBalanceRecord).
 * We use [isEmpty] function in the reducers to filter out such events.
 */
val SolanaEntity<*, *, *>.isEmpty: Boolean get() = createdAt == Instant.EPOCH