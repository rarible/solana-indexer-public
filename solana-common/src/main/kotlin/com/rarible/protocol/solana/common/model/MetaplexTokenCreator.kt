package com.rarible.protocol.solana.common.model

data class MetaplexTokenCreator(
    val address: String,
    val share: Int,
    // TODO[compat]: 'null' only to be compatible with previous messages.
    //  Make not null before deploying.
    val verified: Boolean? = null
)
