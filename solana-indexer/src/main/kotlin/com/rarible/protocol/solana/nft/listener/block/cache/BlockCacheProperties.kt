package com.rarible.protocol.solana.nft.listener.block.cache

import org.springframework.boot.autoconfigure.mongo.MongoProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "block.cache")
data class BlockCacheProperties(
    val rpcApiUrls: List<String> = listOf(),
    val timeout: Long = 30000,
    val batchSize: Int = 200,
    val mongo: MongoProperties?,
)