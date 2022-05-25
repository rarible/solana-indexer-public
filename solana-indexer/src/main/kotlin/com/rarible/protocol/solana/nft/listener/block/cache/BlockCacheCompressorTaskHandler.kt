package com.rarible.protocol.solana.nft.listener.block.cache

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import com.fasterxml.jackson.module.kotlin.readValue
import com.rarible.blockchain.scanner.solana.client.dto.ApiResponse
import com.rarible.blockchain.scanner.solana.client.dto.SolanaBlockDto
import com.rarible.core.apm.withTransaction
import com.rarible.core.task.TaskHandler
import com.rarible.solana.block.BlockCompressor
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

/**
 * Background task handler that iterates by cached Solana blocks in the 'block_cache' database
 * and tries to [compress][BlockCompressor] them to reduce Mongo storage usage.
 *
 * To run this task, add the following JSON to the `task` collection.
 * Replace `_id` with some unique ObjectId
 * `<FROM>` - starting block number, `<TO>` - ending block number.
 *
 * ```
 * {
 *   "_id": {
 *     "$oid": "6242c21bfe476626861a2d97"
 *   },
 *   "type": "BLOCK_CACHE_COMPRESSOR",
 *   "param": "<FROM>",
 *   "state": {
 *     "$numberLong": "<TO>"
 *   },
 *   "running": false,
 *   "lastStatus": "NONE",
 *   "version": {
 *     "$numberLong": "1"
 *   },
 *   "_class": "com.rarible.core.task.Task"
 * }
 * ```
 */
@Component
class BlockCacheCompressorTaskHandler(
    private val repository: BlockCacheRepository,
    private val blockCacheProperties: BlockCacheProperties,
    private val meterRegistry: MeterRegistry,
    private val solanaBlockCompressor: BlockCompressor
) : TaskHandler<Long> {

    private val logger = LoggerFactory.getLogger(javaClass)

    private val mapper = jacksonMapperBuilder()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .build()

    private val blockCacheCompressorTimer by lazy { meterRegistry.timer("block_cache_compressor") }

    override val type: String = "BLOCK_CACHE_COMPRESSOR"

    override fun runLongTask(from: Long?, param: String): Flow<Long> {
        return getRangesReversed(param.toLong(), from!!, blockCacheProperties.batchSize).asFlow()
            .map {
                logger.info("Compressing cached blocks range $it")
                withTransaction("compressCachedBlocks", labels = listOf("blocks" to it.toString())) {
                    compressBlocksRange(it)
                }
                it.minOf { v -> v }
            }
    }

    private suspend fun compressBlocksRange(blocks: LongRange) {
        coroutineScope {
            val loadStartTime = Timer.start()
            blocks.map { blockNumber ->
                async {
                    val logPrefix = "Block cache compressor"
                    logger.info("$logPrefix: loading block $blockNumber")
                    val cachedBlockBytes = repository.find(blockNumber) ?: return@async

                    val cachedBlockResponse = mapper.readValue<ApiResponse<SolanaBlockDto>>(cachedBlockBytes)
                    if (cachedBlockResponse.result == null) {
                        logger.info("$logPrefix: block $blockNumber had error ${cachedBlockResponse.error}, removing it")
                        repository.delete(blockNumber)
                        return@async
                    }
                    if (cachedBlockResponse.result!!.transactions.any { it.transaction == null }) {
                        logger.info("$logPrefix: block $blockNumber is already compressed, skipping it")
                        return@async
                    }

                    logger.info("$logPrefix: compressing block $blockNumber")
                    val compressedResponse = solanaBlockCompressor.compress(cachedBlockResponse)
                    val compressedBytes = mapper.writeValueAsBytes(compressedResponse)
                    val compressionRation = (cachedBlockBytes.size.toDouble() / (compressedBytes.size + 1) * 100).toInt()
                    logger.info("$logPrefix: block $blockNumber was compressed from ${cachedBlockBytes.size} to ${compressedBytes.size} bytes ($compressionRation%)")
                    repository.save(blockNumber, compressedBytes)
                }
            }.awaitAll()
            logger.info(
                "Compressed ${blocks.count()} blocks in ${
                    loadStartTime.stop(blockCacheCompressorTimer).nanoToMillis()
                }"
            )
        }
    }

    private fun Long.nanoToMillis() = TimeUnit.NANOSECONDS.toMillis(this)
}