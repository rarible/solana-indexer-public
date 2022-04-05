package com.rarible.protocol.solana.nft.listener.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

internal const val RARIBLE_ORDER_INDEXER = "order.indexer"

@ConstructorBinding
@ConfigurationProperties(RARIBLE_ORDER_INDEXER)
data class OrderIndexerProperties(
    val orderBalanceConsumerWorkers: Int = 4,
    val orderBalanceConsumerBatchSize: Int = 50
)