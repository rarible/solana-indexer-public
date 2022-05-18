package com.rarible.protocol.solana.common.filter.token

class StaticSolanaBlackListTokenFilter(
    private val blacklistedTokens: Set<String>
) : SolanaTokenFilter {

    override suspend fun isAcceptableToken(mint: String): Boolean = mint !in blacklistedTokens

    override suspend fun addToBlacklist(mints: Collection<String>, reason: String) = Unit
}