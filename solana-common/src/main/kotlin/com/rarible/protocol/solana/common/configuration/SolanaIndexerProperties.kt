package com.rarible.protocol.solana.common.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

const val SOLANA_INDEXER_PROPERTIES_PATH = "common"
const val SOLANA_INDEXER_FEATURE_FLAGS = "common.featureFlags"

@ConstructorBinding
@ConfigurationProperties(SOLANA_INDEXER_PROPERTIES_PATH)
data class SolanaIndexerProperties(
    val kafkaReplicaSet: String,
    val metricRootPath: String,
    val confirmationBlocks: Int = 100,
    val metaplexOffChainMetaLoadingTimeout: Long = 20000,
    val featureFlags: FeatureFlags = FeatureFlags()
)

data class FeatureFlags(
    val enableCacheApi: Boolean = false,
    val tokenFilter: TokenFilterType = TokenFilterType.BLACKLIST,
    val blacklistTokens: Set<String> = emptySet(),
    val auctionHouses: Set<String> = emptySet(),
    /**
     * Flag indicating that we are indexing from the blockchain beginning.
     * Currently, we index only the 'mainnet-beta' from block #80KK (consider it is old enough),
     * but 'dev' and 'staging' (devnet) we're indexing from a random instant (like 3 months ago),
     * so we could have not seen balance/token initialization records.
     */
    val isIndexingFromBeginning: Boolean = true,
    val skipTokensWithoutMeta: Boolean = false,
    val tokenFilterInMemoryCacheSize: Long = 1_000_000
)

enum class TokenFilterType {
    WHITELIST,
    WHITELIST_V2,
    BLACKLIST
}
