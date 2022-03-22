package com.rarible.protocol.solana.nft.listener.service.subscribers.filter

class SolanaWhiteListTokenFilter(
    private val tokens: Set<String>
) : SolanaTokenFilter {

    override fun isAcceptableToken(mint: String): Boolean {
        return tokens.contains(mint)
    }
}