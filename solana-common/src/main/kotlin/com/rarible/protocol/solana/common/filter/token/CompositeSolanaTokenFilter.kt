package com.rarible.protocol.solana.common.filter.token

class CompositeSolanaTokenFilter(
    private val filters: List<SolanaTokenFilter>
) : SolanaTokenFilter {
    override suspend fun isAcceptableToken(mint: String): Boolean =
        filters.all { it.isAcceptableToken(mint) }

    override suspend fun addToBlacklist(mints: Collection<String>, reason: String) {
        filters.forEach { it.addToBlacklist(mints, reason) }
    }
}