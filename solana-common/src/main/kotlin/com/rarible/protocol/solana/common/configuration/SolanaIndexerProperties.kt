package com.rarible.protocol.solana.common.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

const val SOLANA_INDEXER_PROPERTIES_PATH = "common"

@ConstructorBinding
@ConfigurationProperties(SOLANA_INDEXER_PROPERTIES_PATH)
data class SolanaIndexerProperties(
    val kafkaReplicaSet: String,
    val metricRootPath: String,
    val confirmationBlocks: Int = 100,
    val featureFlags: FeatureFlags = FeatureFlags()
)

data class FeatureFlags(
    val skipTransfersWithUnknownMint: Boolean = false,
    val enableInMemoryLogRecordHandling: Boolean = false,
    val skipInMemoryLogRecordHandling: Boolean = false,
    val enableAccountToMintAssociationCache: Boolean = true
)
