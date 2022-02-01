package com.rarible.protocol.solana.nft.listener.configuration

import com.rarible.core.daemon.DaemonWorkerProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

const val RARIBLE_PROTOCOL_NFT_INDEXER = "common"

@ConstructorBinding
@ConfigurationProperties(RARIBLE_PROTOCOL_NFT_INDEXER)
data class NftIndexerProperties(
    val kafkaReplicaSet: String,
    val maxPollRecords: Int = 100,
    val metricRootPath: String,
    val daemonWorkerProperties: DaemonWorkerProperties = DaemonWorkerProperties(),
    val confirmationBlocks: Int = 100,
    val metadataProgramId: String = "metaqbxxUerdq28cj1RbAWkYQm3ybzjb6a8bt518x1s"
)