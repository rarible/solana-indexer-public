package com.rarible.protocol.solana.nft.listener.block.cache

import org.bson.types.Binary
import org.springframework.data.annotation.Id

data class BlockCache(
    @Id
    val id: Long,
    val data: Binary,
)