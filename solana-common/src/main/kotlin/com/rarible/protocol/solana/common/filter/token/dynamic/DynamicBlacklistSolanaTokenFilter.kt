package com.rarible.protocol.solana.common.filter.token.dynamic

import com.rarible.protocol.solana.common.filter.token.SolanaTokenFilter
import com.rarible.protocol.solana.common.repository.DynamicBlacklistedTokenRepository

@Suppress("UnstableApiUsage")
class DynamicBlacklistSolanaTokenFilter(
    private val dynamicBlacklistedTokenRepository: DynamicBlacklistedTokenRepository
) : SolanaTokenFilter {

    override suspend fun isAcceptableToken(mint: String): Boolean =
        dynamicBlacklistedTokenRepository.findAll(listOf(mint)).isNotEmpty()

    override suspend fun addToBlacklist(mints: Collection<String>, reason: String) {
        dynamicBlacklistedTokenRepository.saveAll(mints, reason)
    }
}