package com.rarible.protocol.solana.common.model

/**
 * Metaplex on-chain metadata of the token.
 */
data class MetaplexTokenMeta(
    val name: String,
    val symbol: String,
    val uri: String,
    val sellerFeeBasisPoints: Int,
    val creators: List<MetaplexTokenCreator>?,
    val mutable: Boolean,
    val collection: Collection?
) {
    data class Collection(
        val address: String,
        val verified: Boolean
    )
}
