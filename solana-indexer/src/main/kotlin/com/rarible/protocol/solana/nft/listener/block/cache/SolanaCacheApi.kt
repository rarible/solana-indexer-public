package com.rarible.protocol.solana.nft.listener.block.cache

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import com.fasterxml.jackson.module.kotlin.readValue
import com.rarible.blockchain.scanner.solana.client.SolanaApi
import com.rarible.blockchain.scanner.solana.client.SolanaHttpRpcApi
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

class SolanaCacheApi(
    private val repository: BlockCacheRepository,
    private val httpApi: SolanaHttpRpcApi,
    private val properties: BlockCacheProperties,
    meterRegistry: MeterRegistry
) : SolanaApi {

    private val mapper = jacksonMapperBuilder()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .build()

    private val blockCacheFetchTimer = Timer
        .builder(BLOCK_CACHE_TIMER)
        .register(meterRegistry)

    private val blockCacheBatchFetchTimer = Timer
        .builder(BLOCK_CACHE_BATCH_TIMER)
        .register(meterRegistry)

    private val blockCacheLoadedSize = Counter
        .builder(BLOCK_CACHE_LOADED_SIZE)
        .register(meterRegistry)

    private val blockCacheHits = Counter
        .builder(BLOCK_CACHE_HITS)
        .register(meterRegistry)

    private val blockCacheMisses = Counter
        .builder(BLOCK_CACHE_MISSES)
        .register(meterRegistry)

    private var lastKnownBlock: Long = 0
    private var lastKnownBlockUpdated: Instant = Instant.EPOCH

    override suspend fun getBlock(
        slot: Long,
        details: GetBlockRequest.TransactionDetails
    ): ApiResponse<SolanaBlockDto> {
        return if (details == GetBlockRequest.TransactionDetails.None) {
            httpApi.getBlock(slot, details)
        } else {
            val fetchStart = Timer.start()
            val fromCache = getFromCache(slot)

            fetchStart.stop(blockCacheFetchTimer)
            parseBlock(slot, fromCache, details)
        }
    }

    private suspend fun parseBlock(
        slot: Long,
        block: ByteArray?,
        details: GetBlockRequest.TransactionDetails
    ): ApiResponse<SolanaBlockDto> {
        return if (block != null) {
            blockCacheHits.increment()
            blockCacheLoadedSize.increment(block.size.toDouble())
            mapper.readValue(block)
        } else {
            blockCacheMisses.increment()
            val result = httpApi.getBlock(slot, details)
            if (shouldSaveBlockToCache(slot)) {
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
        return if (properties.enableBatch) {
            return if (details == GetBlockRequest.TransactionDetails.None) {
                slots.associateWith { httpApi.getBlock(it, details) }
            } else {
                val fetchStart = Timer.start()
                val slotToBlock = getFromCache(slots)
                fetchStart.stop(blockCacheBatchFetchTimer)

                coroutineScope {
                    slots.map { slot ->
                        async {
                            val block = slotToBlock[slot]

                            slot to parseBlock(slot, block, details)
                        }
                    }.awaitAll().toMap()
                }
            }
        } else {
            coroutineScope {
                slots.map {
                    async { it to getBlock(it, details) }
                }.awaitAll().toMap()
            }
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

    private suspend fun getFromCache(slots: List<Long>): Map<Long, ByteArray?> {
        return repository.findAll(slots).mapValues { it.value.takeIf(ByteArray::isCorrect) }
    }

    private suspend fun getFromCache(slot: Long): ByteArray? {
        val fromCache = repository.find(slot) ?: return null
        if (!fromCache.isCorrect()) {
            logger.info("block cache {} was incorrect. reloading", slot)
            return null
        }
        return fromCache
    }

    override suspend fun getFirstAvailableBlock(): ApiResponse<Long> = httpApi.getFirstAvailableBlock()

    override suspend fun getLatestSlot(): ApiResponse<Long> = httpApi.getLatestSlot()

    override suspend fun getTransaction(signature: String): ApiResponse<SolanaTransactionDto> =
        httpApi.getTransaction(signature)

    private companion object {
        val logger: Logger = LoggerFactory.getLogger(SolanaCacheApi::class.java)
        const val BLOCK_CACHE_BATCH_TIMER = "block_cache_batch_fetch_timer"
        const val BLOCK_CACHE_TIMER = "block_cache_fetch_timer"
        const val BLOCK_CACHE_LOADED_SIZE = "block_cache_loaded_size"
        const val BLOCK_CACHE_HITS = "block_cache_hits"
        const val BLOCK_CACHE_MISSES = "block_cache_misses"
    }
}

/**
 * Some cached blocks contains such data:
 * ```{"jsonrpc":"2.0","result":null,"id":1}```
 *
 * This is probably a temporary response by Solana RPC Nodes until the block is indexed by the node.
 * We need to ignore such cached blocks and reload them.
 */
private val emptyBlock = Base64.getDecoder().decode("eyJqc29ucnBjIjoiMi4wIiwicmVzdWx0IjpudWxsLCJpZCI6MX0K")

private fun ByteArray.isCorrect(): Boolean {
    if (size <= 2) return false
    return !this.contentEquals(emptyBlock)
}