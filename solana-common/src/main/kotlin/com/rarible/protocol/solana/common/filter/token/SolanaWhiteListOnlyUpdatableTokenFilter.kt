package com.rarible.protocol.solana.common.filter.token

class SolanaWhiteListOnlyUpdatableTokenFilter(
    private val whitelistMints: Set<String>
) : SolanaTokenFilter {

    override suspend fun isAcceptableToken(mint: String): Boolean = true

    override suspend fun isAcceptableForUpdateToken(mint: String): Boolean = mint in whitelistMints

    override suspend fun addToBlacklist(mintsAndReasons: Map<String, String>) = Unit
}