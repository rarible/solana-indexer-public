package com.rarible.protocol.solana.common.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(MetaplexOffChainMeta.COLLECTION)
data class MetaplexOffChainMeta(
    @Id
    val tokenAddress: String,
    val metaFields: MetaplexOffChainMetaFields,
    val loadedAt: Instant
) {
    companion object {
        const val COLLECTION = "metaplex-off-chain-meta"
    }
}
