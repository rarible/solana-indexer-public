package com.rarible.protocol.solana.common.model

/**
 * Metaplex off-chain meta fields according to spec 1.0.0
 * (see https://docs.metaplex.com/token-metadata/Versions/v1.0.0/nft-standard#uri-json-schema).
 *
 * Some fields may be missing, we will add them gradually.
 */
data class MetaplexOffChainMetaFields(
    val name: String,
    val symbol: String,
    val description: String,
    val collection: Collection?,
    val sellerFeeBasisPoints: Int?,
    val externalUrl: String?,
    val edition: String?,
    val backgroundColor: String?,
    val attributes: List<Attribute>,
    val properties: Properties?,
    val image: String?,
    val animationUrl: String?
) {
    data class Collection(
        val name: String,
        val family: String,
        val hash: String
    )

    data class Attribute(
        val traitType: String?,
        val value: String?
    )

    data class Properties(
        val category: String?,
        val creators: List<MetaplexTokenCreator>?,
        val files: List<File>?
    ) {
        data class File(
            val uri: String?,
            val type: String?
        )
    }
}
