package com.rarible.protocol.solana.nft.listener.block.cache

import org.springframework.boot.autoconfigure.mongo.MongoProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "block.cache")
data class BlockCacheProperties(
    val batchSize: Int = 200,
    val mongo: MongoProperties?,
    val enableBatch: Boolean = false,
    val batchSave: Boolean = false
)