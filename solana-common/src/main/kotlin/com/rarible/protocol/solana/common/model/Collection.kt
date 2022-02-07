package com.rarible.protocol.solana.common.model

sealed class Collection {
    data class JsonCollection(
        val name: String,
        val family: String,
        val hash: String
    ) : Collection()

    data class OnChainCollection(
        val address: String,
        val verified: Boolean
    ) : Collection()
}
