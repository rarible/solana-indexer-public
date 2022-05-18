package com.rarible.protocol.solana.common.filter.token.dynamic

import com.rarible.core.common.nowMillis
import com.rarible.protocol.solana.common.filter.token.dynamic.DynamicBlacklistedTokenEntry.Companion.COLLECTION
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(COLLECTION)
data class DynamicBlacklistedTokenEntry(
    @Id
    val mint: String,
    val reason: String?,
    val updatedAt: Instant? = nowMillis()
) {
    companion object {
        const val COLLECTION = "tokens-blacklist"
    }
}