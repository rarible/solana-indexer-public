package com.rarible.protocol.solana.common.configuration

import com.rarible.protocol.solana.common.converter.PackageConverters
import com.rarible.protocol.solana.common.filter.token.CompositeSolanaTokenFilter
import com.rarible.protocol.solana.common.filter.token.CurrencyTokenReader
import com.rarible.protocol.solana.common.filter.token.TokenListFileReader
import com.rarible.protocol.solana.common.filter.token.SolanaBlackListTokenFilter
import com.rarible.protocol.solana.common.filter.token.SolanaTokenFilter
import com.rarible.protocol.solana.common.filter.token.SolanaWhiteListTokenFilter
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import java.time.Clock

@Configuration
@Import(
    RepositoryConfiguration::class,
    EventProducerConfiguration::class,
    SolanaMetaConfiguration::class,
    SolanaServiceConfiguration::class
)
@EnableConfigurationProperties(SolanaIndexerProperties::class)
@ComponentScan(basePackageClasses = [PackageConverters::class])
class CommonConfiguration(
    private val properties: SolanaIndexerProperties
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Bean
    fun solanaClock(): Clock = Clock.systemUTC()

    @Bean
    fun featureFlags(): FeatureFlags {
        logger.info("Activated feature flags: {}", properties.featureFlags)
        return properties.featureFlags
    }

    @Bean
    fun tokenFilter(featureFlags: FeatureFlags): SolanaTokenFilter {
        val currencyMints = CurrencyTokenReader().readCurrencyTokens().tokens.mapTo(hashSetOf()) { it.address }
        val nonCurrencySolanaTokenFilter = SolanaBlackListTokenFilter(currencyMints)
        val additionalTokenFilter = when (featureFlags.tokenFilter) {
            TokenFilterType.NONE -> {
                object : SolanaTokenFilter {
                    override fun isAcceptableToken(mint: String): Boolean = true
                }
            }
            TokenFilterType.WHITELIST -> {
                val tokens = TokenListFileReader("/whitelist").readTokens(WHITELIST_FILES)
                SolanaWhiteListTokenFilter(tokens)
            }
            TokenFilterType.BLACKLIST -> {
                val tokens = TokenListFileReader("/blacklist").readTokens(BLACKLIST_FILES) + featureFlags.blacklistTokens
                SolanaBlackListTokenFilter(tokens)
            }
        }
        return CompositeSolanaTokenFilter(listOf(nonCurrencySolanaTokenFilter, additionalTokenFilter))
    }

    private companion object {
        val WHITELIST_FILES = listOf(
            "degenape",
            "degenerate_ape_kindergarten",
            "degeneratetrashpandas"
        )

        val BLACKLIST_FILES = emptyList<String>()
    }
}
