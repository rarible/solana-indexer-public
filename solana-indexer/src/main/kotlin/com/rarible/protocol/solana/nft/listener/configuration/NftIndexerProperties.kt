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
    val nftItemMetaExtenderWorkersCount: Int = 4,
    val daemonWorkerProperties: DaemonWorkerProperties = DaemonWorkerProperties(),
    val nftCollectionMetaExtenderWorkersCount: Int = 4,
    val confirmationBlocks: Int = 12,
    val ownershipSaveBatch: Int = 20
)