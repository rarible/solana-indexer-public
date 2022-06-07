package com.rarible.protocol.solana.common.model


/**
 * Metaplex on-chain metadata of the token.
 */
data class MetaplexMetaFields(
    val name: String,
    val symbol: String,
    val uri: String,
    val sellerFeeBasisPoints: Int,
    val creators: List<MetaplexTokenCreator>,
    val collection: Collection?
) {
    data class Collection(
        val address: String,
        val verified: Boolean
    )
}

fun MetaplexMetaFields.merge(other: MetaplexMetaFields?): MetaplexMetaFields {
    if (other == null) return this

    return MetaplexMetaFields(
        other.name,
        other.symbol,
        other.uri,
        other.sellerFeeBasisPoints,
        other.creators.ifEmpty { this.creators },
        other.collection ?: this.collection
    )
}

fun String.trimEndNulls(): String = trimEnd(0.toChar())
