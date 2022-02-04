package com.rarible.protocol.solana.common.meta

import com.rarible.loader.cache.CacheLoader
import com.rarible.loader.cache.CacheType
import com.rarible.protocol.solana.common.model.TokenId
import org.springframework.stereotype.Component
import java.net.URL

@Component
class SolanaMetaCacheLoader(
    private val solanaMetaLoader: SolanaMetaLoader
) : CacheLoader<SolanaMeta> {

    override val type: CacheType get() = TYPE

    override suspend fun load(key: String): SolanaMeta {
        val (tokenAddress, url) = decodeKey(key)
        return solanaMetaLoader.loadMeta(tokenAddress, url)
    }

    companion object {
        val TYPE: CacheType = "solana-token-meta"

        fun encodeKey(tokenAddress: TokenId, metadataUrl: URL): String =
            "$tokenAddress:${metadataUrl.toExternalForm()}"

        fun decodeKey(key: String): Pair<TokenId, URL> =
            key.substringBefore(":") to URL(key.substringAfter(":"))
    }
}
