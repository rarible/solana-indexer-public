package com.rarible.protocol.solana.common.filter.token

interface SolanaTokenFilter {

    suspend fun isAcceptableToken(mint: String): Boolean

    suspend fun isAcceptableForUpdateToken(mint: String): Boolean = isAcceptableToken(mint)

    suspend fun addToBlacklist(mintsAndReasons: Map<String, String>)

}