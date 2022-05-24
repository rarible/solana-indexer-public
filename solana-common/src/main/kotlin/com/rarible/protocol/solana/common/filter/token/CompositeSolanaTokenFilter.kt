package com.rarible.protocol.solana.common.filter.token

class CompositeSolanaTokenFilter(
    private val filters: List<SolanaTokenFilter>
) : SolanaTokenFilter {
    override suspend fun isAcceptableToken(mint: String): Boolean =
        filters.all { it.isAcceptableToken(mint) }

    override suspend fun isAcceptableForUpdateToken(mint: String): Boolean =
        filters.all { it.isAcceptableForUpdateToken(mint) }

    override suspend fun addToBlacklist(mintsAndReasons: Map<String, String>) {
        filters.forEach { it.addToBlacklist(mintsAndReasons) }
    }
}