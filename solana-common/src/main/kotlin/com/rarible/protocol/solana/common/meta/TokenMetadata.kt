package com.rarible.protocol.solana.common.meta

import com.rarible.protocol.solana.common.model.MetaplexTokenCreator

/**
 * Metaplex aggregated meta of on-chain and off-chain meta.
 */
data class TokenMetadata(
    val name: String,
    val symbol: String,
    val description: String,
    val creators: List<MetaplexTokenCreator>,
    val collection: Collection?,
    val url: String
) {
    sealed class Collection {
        data class OnChain(
            val address: String,
            val verified: Boolean
        ) : Collection()

        data class OffChain(
            val name: String,
            val family: String,
            val hash: String
        ) : Collection()
    }
}
