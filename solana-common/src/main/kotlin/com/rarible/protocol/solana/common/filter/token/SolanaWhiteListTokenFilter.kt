package com.rarible.protocol.solana.common.filter.token

class SolanaWhiteListTokenFilter(
    private val tokens: Set<String>
) : SolanaTokenFilter {

    override fun isAcceptableToken(mint: String): Boolean {
        return tokens.contains(mint)
    }
}