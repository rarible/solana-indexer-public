package com.rarible.protocol.solana.common.filter.token

import com.google.common.cache.CacheBuilder

@Suppress("UnstableApiUsage")
class InMemoryCachingSolanaTokenFilter(
    private val delegate: SolanaTokenFilter,
    cacheMaxSize: Long
) : SolanaTokenFilter {

    private val acceptableCache = CacheBuilder
        .newBuilder()
        .maximumSize(cacheMaxSize)
        .build<String, Boolean /* True if acceptable, False otherwise, Null if unknown */>()

    override suspend fun isAcceptableToken(mint: String): Boolean {
        val cachedIsAcceptable = acceptableCache.getIfPresent(mint)
        if (cachedIsAcceptable != null) {
            return cachedIsAcceptable
        }
        val isAcceptable = delegate.isAcceptableToken(mint)
        acceptableCache.put(mint, isAcceptable)
        return isAcceptable
    }

    override suspend fun addToBlacklist(mintsAndReasons: Map<String, String>) {
        delegate.addToBlacklist(mintsAndReasons)
        for (mint in mintsAndReasons.keys) {
            acceptableCache.put(mint, false)
        }
    }
}