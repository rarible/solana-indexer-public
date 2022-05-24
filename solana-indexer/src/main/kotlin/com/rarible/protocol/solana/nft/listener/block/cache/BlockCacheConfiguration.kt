package com.rarible.protocol.solana.nft.listener.block.cache

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(BlockCacheProperties::class)
class BlockCacheConfiguration