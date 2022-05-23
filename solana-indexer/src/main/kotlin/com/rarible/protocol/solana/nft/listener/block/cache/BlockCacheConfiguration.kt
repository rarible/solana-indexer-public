package com.rarible.protocol.solana.nft.listener.block.cache

import com.rarible.protocol.solana.common.configuration.SolanaIndexerProperties
import com.rarible.solana.block.SolanaBlockCompressingApi
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(BlockCacheProperties::class)
class BlockCacheConfiguration {
    @Bean
    fun blockCacheClient(
        solanaIndexerProperties: SolanaIndexerProperties,
        @Qualifier("solanaBlockCompressingApi") solanaBlockCompressingApi: SolanaBlockCompressingApi
    ): BlockCacheClient = BlockCacheClient(solanaApi = solanaBlockCompressingApi)
}