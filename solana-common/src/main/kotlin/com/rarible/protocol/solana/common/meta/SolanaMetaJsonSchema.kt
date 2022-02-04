package com.rarible.protocol.solana.common.meta

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class SolanaMetaJsonSchema(
    val name: String,
    val symbol: String,
    val description: String,
    val seller_fee_basis_points: Int,
    val external_url: String,
    val edition: String,
    val background_color: String,
    val attributes: List<Attribute>,
    val properties: Properties,
    val image: String
) {
    data class Attribute(
        val trait_type: String,
        val value: String
    )

    data class Properties(
        val category: String,
        val creators: List<Creator>,
        val files: List<File>
    ) {
        data class Creator(
            val address: String,
            val share: Int
        )

        data class File(
            val uri: String,
            val type: String
        )
    }
}
