package com.rarible.protocol.solana.common.filter.token.dynamic

import com.rarible.protocol.solana.common.filter.token.SolanaTokenFilter
import com.rarible.protocol.solana.common.repository.DynamicBlacklistedTokenRepository

@Suppress("UnstableApiUsage")
class DynamicBlacklistSolanaTokenFilter(
    private val dynamicBlacklistedTokenRepository: DynamicBlacklistedTokenRepository,
    /**
     * Feature flag, if `true` the filtering will work as expected,
     * otherwise, mints will be black-listed but filter will still accept them.
     */
    private val featureFlagEnableDynamicFiltering: Boolean = true
) : SolanaTokenFilter {

    override suspend fun isAcceptableToken(mint: String): Boolean {
        return if (featureFlagEnableDynamicFiltering) {
            dynamicBlacklistedTokenRepository.findAll(listOf(mint)).isEmpty()
        } else {
            true
        }
    }

    override suspend fun addToBlacklist(mints: Collection<String>, reason: String) {
        dynamicBlacklistedTokenRepository.saveAll(mints, reason)
    }
}