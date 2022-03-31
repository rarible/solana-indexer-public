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
    val tokenFilter: TokenFilterType = TokenFilterType.NONE,
    val blacklistTokens: Set<String> = emptySet()
)

enum class TokenFilterType {
    WHITELIST,
    BLACKLIST,
    NONE
}
