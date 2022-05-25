package com.rarible.protocol.solana.common.filter.token

class SolanaWhiteListOnlyUpdatableTokenFilter(
    private val whitelistMints: Set<String>
) : SolanaTokenFilter {

    // Accept all records.
    override suspend fun isAcceptableToken(mint: String): Boolean = true

    // Reduce only whitelisted mints.
    override suspend fun isAcceptableForUpdateToken(mint: String): Boolean = mint in whitelistMints

    override suspend fun addToBlacklist(mintsAndReasons: Map<String, String>) = Unit
}