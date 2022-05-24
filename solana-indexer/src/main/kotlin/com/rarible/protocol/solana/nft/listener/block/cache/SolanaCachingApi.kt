package com.rarible.protocol.solana.nft.listener.block.cache

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import com.fasterxml.jackson.module.kotlin.readValue
import com.rarible.blockchain.scanner.solana.client.SolanaApi
import com.rarible.blockchain.scanner.solana.client.dto.ApiResponse
import com.rarible.blockchain.scanner.solana.client.dto.GetBlockRequest
import com.rarible.blockchain.scanner.solana.client.dto.SolanaBlockDto
import com.rarible.blockchain.scanner.solana.client.dto.SolanaTransactionDto
import com.rarible.core.common.nowMillis
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.Instant
import java.util.*

class SolanaCachingApi(
    private val delegate: SolanaApi,
    private val repository: BlockCacheRepository,
    meterRegistry: MeterRegistry
) : SolanaApi {

    private val mapper = jacksonMapperBuilder()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .build()

    private val metricBlockCacheFetchTimer = Timer.builder(BLOCK_CACHE_TIMER).register(meterRegistry)
    private val metricBlockCacheLoadedSize = Counter.builder(BLOCK_CACHE_LOADED_SIZE).register(meterRegistry)
    private val metricBlockCacheHits = Counter.builder(BLOCK_CACHE_HITS).register(meterRegistry)
    private val metricBlockCacheMisses = Counter.builder(BLOCK_CACHE_MISSES).register(meterRegistry)

    private var lastKnownBlock: Long = 0
    private var lastKnownBlockUpdated: Instant = Instant.EPOCH

    override suspend fun getBlock(
        slot: Long,
        details: GetBlockRequest.TransactionDetails
    ): ApiResponse<SolanaBlockDto> {
        return if (details == GetBlockRequest.TransactionDetails.None) {
            delegate.getBlock(slot, details)
        } else {
            val fetchStart = Timer.start()
            val fromCache = getFromCache(slot)

            fetchStart.stop(metricBlockCacheFetchTimer)
            parseBlock(slot, fromCache, details)
        }
    }

    private suspend fun parseBlock(
        slot: Long,
        block: ByteArray?,
        details: GetBlockRequest.TransactionDetails
    ): ApiResponse<SolanaBlockDto> {
        return if (block != null) {
            metricBlockCacheHits.increment()
            metricBlockCacheLoadedSize.increment(block.size.toDouble())
            mapper.readValue(block)
        } else {
            metricBlockCacheMisses.increment()
            val result = delegate.getBlock(slot, details)
            if (result.result != null && shouldSaveBlockToCache(slot)) {
                val bytes = mapper.writeValueAsBytes(result)
                repository.save(slot, bytes)
            }
            result
        }
    }

    private suspend fun updateLastKnownBlockIfNecessary() {
        if (lastKnownBlock == 0L || Duration.between(lastKnownBlockUpdated, nowMillis()) > Duration.ofHours(1)) {
            lastKnownBlock = getLatestSlot().result ?: lastKnownBlock
            lastKnownBlockUpdated = nowMillis()
        }
    }

    override suspend fun getBlocks(
        slots: List<Long>,
        details: GetBlockRequest.TransactionDetails
    ): Map<Long, ApiResponse<SolanaBlockDto>> {
        return coroutineScope {
            slots.map {
                async { it to getBlock(it, details) }
            }.awaitAll().toMap()
        }
    }

    /**
     * Save the block to the cache only if it is stable enough (approximately >6 hours).
     */
    private suspend fun shouldSaveBlockToCache(slot: Long): Boolean {
        updateLastKnownBlockIfNecessary()
        val shouldSave = lastKnownBlock != 0L && slot < lastKnownBlock - 50000
        if (!shouldSave) {
            logger.info(
                "Do not save the block #$slot to the cache because it may be unstable, " +
                        "last known block #$lastKnownBlock is away ${slot - lastKnownBlock} blocks only"
            )
        }
        return shouldSave
    }

    private suspend fun getFromCache(slot: Long): ByteArray? {
        val fromCache = repository.find(slot) ?: return null
        if (!isCorrectCachedBlock(fromCache)) {
            logger.info("block cache {} was incorrect. reloading", slot)
            return null
        }
        return fromCache
    }

    override suspend fun getFirstAvailableBlock(): ApiResponse<Long> =
        delegate.getFirstAvailableBlock()

    override suspend fun getLatestSlot(): ApiResponse<Long> =
        delegate.getLatestSlot()

    override suspend fun getTransaction(signature: String): ApiResponse<SolanaTransactionDto> =
        delegate.getTransaction(signature)

    /**
     * Some cached blocks contains such data:
     * ```{"jsonrpc":"2.0","id":1}```
     * ```{"jsonrpc":"2.0","result":null,"id":1}```
     *
     * This is probably a temporary response by Solana RPC Nodes until the block is indexed by the node.
     * We need to ignore such cached blocks and reload them.
     */
    private val emptyBlocks = listOf(
        "eyJqc29ucnBjIjoiMi4wIiwiaWQiOjF9",
        "eyJqc29ucnBjIjoiMi4wIiwicmVzdWx0IjpudWxsLCJpZCI6MX0K"
    ).map { Base64.getDecoder().decode(it) }

    private fun isCorrectCachedBlock(bytes: ByteArray): Boolean {
        if (bytes.size <= 2) return false
        return emptyBlocks.none { bytes.contentEquals(it) }
    }

    private companion object {
        val logger: Logger = LoggerFactory.getLogger(SolanaCachingApi::class.java)
        const val BLOCK_CACHE_TIMER = "block_cache_fetch_timer"
        const val BLOCK_CACHE_LOADED_SIZE = "block_cache_loaded_size"
        const val BLOCK_CACHE_HITS = "block_cache_hits"
        const val BLOCK_CACHE_MISSES = "block_cache_misses"
    }
}