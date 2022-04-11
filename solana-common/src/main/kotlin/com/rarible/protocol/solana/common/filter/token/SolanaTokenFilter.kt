package com.rarible.protocol.solana.common.filter.token

interface SolanaTokenFilter {

    fun isAcceptableToken(mint: String): Boolean

}