package com.rarible.protocol.solana.common.meta

import com.rarible.core.loader.LoadTaskStatus
import com.rarible.loader.cache.CacheEntry
import com.rarible.loader.cache.CacheLoaderEvent
import com.rarible.loader.cache.CacheLoaderEventListener
import com.rarible.loader.cache.CacheType
import org.springframework.stereotype.Component

@Component
class SolanaMetaCacheLoaderListener : CacheLoaderEventListener<SolanaMeta> {

    override val type: CacheType get() = SolanaMetaCacheLoader.TYPE

    override suspend fun onEvent(cacheLoaderEvent: CacheLoaderEvent<SolanaMeta>) {
        // TODO[meta]: send TokenUpdateEvent notification.
        // TODO[meta]: change Token's collection.
        // TODO[meta]: add the tests for these cases.
        val (tokenAddress, _) = SolanaMetaCacheLoader.decodeKey(cacheLoaderEvent.key)
        when (val cacheEntry = cacheLoaderEvent.cacheEntry) {
            is CacheEntry.Loaded -> {
                logMetaLoading(tokenAddress, "loaded meta")
            }
            is CacheEntry.InitialFailed -> {
                logMetaLoading(tokenAddress, "initial loading failed: ${cacheEntry.failedStatus.errorMessage}")
            }
            is CacheEntry.LoadedAndUpdateFailed -> {
                logMetaLoading(tokenAddress, "update failed: ${cacheEntry.failedUpdateStatus.errorMessage}")
            }
            is CacheEntry.LoadedAndUpdateScheduled -> {
                logMetaLoading(tokenAddress, "update was scheduled")
            }
            is CacheEntry.InitialLoadScheduled -> {
                when (cacheEntry.loadStatus) {
                    is LoadTaskStatus.Scheduled -> {
                        logMetaLoading(tokenAddress, "loading scheduled")
                    }
                    is LoadTaskStatus.WaitsForRetry -> {
                        logMetaLoading(tokenAddress, "loading has failed and started waiting for retry")
                    }
                }
            }
            is CacheEntry.NotAvailable -> {
                logMetaLoading(tokenAddress, "meta was removed")
            }
        }
    }
}
