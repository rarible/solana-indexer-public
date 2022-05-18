package com.rarible.protocol.solana.common.filter.token

class SolanaWhiteListTokenFilter(
    private val tokens: Set<String>
) : SolanaTokenFilter {

    override suspend fun isAcceptableToken(mint: String): Boolean = mint in tokens

    override suspend fun addToBlacklist(mints: Collection<String>, reason: String) = Unit
}