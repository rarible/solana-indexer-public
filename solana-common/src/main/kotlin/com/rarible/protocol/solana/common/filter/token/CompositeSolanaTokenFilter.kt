package com.rarible.protocol.solana.common.filter.token

class CompositeSolanaTokenFilter(
    private val filters: List<SolanaTokenFilter>
) : SolanaTokenFilter {
    override fun isAcceptableToken(mint: String): Boolean =
        filters.all { it.isAcceptableToken(mint) }
}