package com.rarible.protocol.solana.nft.listener.block.cache

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.rarible.blockchain.scanner.solana.client.SolanaApi
import com.rarible.blockchain.scanner.solana.client.SolanaHttpRpcApi
import com.rarible.blockchain.scanner.solana.client.dto.ApiResponse
import com.rarible.blockchain.scanner.solana.client.dto.GetBlockRequest
import com.rarible.blockchain.scanner.solana.client.dto.SolanaBlockDto
import com.rarible.blockchain.scanner.solana.client.dto.SolanaTransactionDto
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer

class SolanaCacheApi(
    private val repository: BlockCacheRepository,
    private val httpApi: SolanaHttpRpcApi,
    private val mapper: ObjectMapper,
    meterRegistry: MeterRegistry
) : SolanaApi {

    private val blockCacheFetchTimer = Timer
        .builder(BLOCK_CACHE_TIMER)
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

    override suspend fun getBlock(
        slot: Long,
        details: GetBlockRequest.TransactionDetails
    ): ApiResponse<SolanaBlockDto> {
        val fetchStart = Timer.start()
        val fromCache = repository.find(slot)
        fetchStart.stop(blockCacheFetchTimer)
        if (fromCache != null) {
            blockCacheHits.increment()
            blockCacheLoadedSize.increment(fromCache.size.toDouble())
        } else {
            blockCacheMisses.increment()
        }
        val bytes = fromCache ?: return httpApi.getBlock(slot, details)
        val apiResponse = mapper.readValue<ApiResponse<SolanaBlockDto>>(bytes)

        return when (details) {
            GetBlockRequest.TransactionDetails.Full -> apiResponse
            GetBlockRequest.TransactionDetails.None -> apiResponse.copy(
                error = apiResponse.error,
                result = apiResponse.result?.copy(transactions = emptyList())
            )
        }
    }

    override suspend fun getFirstAvailableBlock(): ApiResponse<Long> = httpApi.getFirstAvailableBlock()

    override suspend fun getLatestSlot(): ApiResponse<Long> = httpApi.getLatestSlot()

    override suspend fun getTransaction(signature: String): ApiResponse<SolanaTransactionDto> = httpApi.getTransaction(signature)

    private companion object {
        const val BLOCK_CACHE_TIMER = "block_cache_fetch_timer"
        const val BLOCK_CACHE_LOADED_SIZE = "block_cache_loaded_size"
        const val BLOCK_CACHE_HITS = "block_cache_hits"
        const val BLOCK_CACHE_MISSES = "block_cache_misses"
    }
}