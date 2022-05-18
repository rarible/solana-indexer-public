package com.rarible.protocol.solana.common.filter.token

interface SolanaTokenFilter {

    suspend fun isAcceptableToken(mint: String): Boolean

    suspend fun addToBlacklist(mints: Collection<String>, reason: String)

}