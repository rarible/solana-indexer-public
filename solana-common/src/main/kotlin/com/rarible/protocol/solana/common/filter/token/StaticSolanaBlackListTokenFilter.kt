package com.rarible.protocol.solana.common.filter.token

class StaticSolanaBlackListTokenFilter(
    private val blacklistedTokens: Set<String>
) : SolanaTokenFilter {

    override suspend fun isAcceptableToken(mint: String): Boolean = mint !in blacklistedTokens

    override suspend fun isAcceptableForUpdateToken(mint: String): Boolean = true

    override suspend fun addToBlacklist(mintsAndReasons: Map<String, String>) = Unit
}