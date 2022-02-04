package com.rarible.protocol.solana.common.configuration

import com.rarible.loader.cache.CacheLoaderService
import com.rarible.loader.cache.configuration.EnableRaribleCacheLoader
import com.rarible.protocol.solana.common.meta.SolanaMeta
import com.rarible.protocol.solana.common.meta.SolanaMetaCacheLoader
import com.rarible.protocol.solana.common.meta.SolanaMetaLoader
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan

@EnableRaribleCacheLoader
@ComponentScan(basePackageClasses = [SolanaMetaLoader::class])
class SolanaMetaConfiguration {
    @Bean
    fun solanaMetaCacheLoaderService(
        cacheLoaderServices: List<CacheLoaderService<*>>
    ): CacheLoaderService<SolanaMeta> {
        @Suppress("UNCHECKED_CAST")
        return cacheLoaderServices.find { it.type == SolanaMetaCacheLoader.TYPE }
                as CacheLoaderService<SolanaMeta>
    }
}
