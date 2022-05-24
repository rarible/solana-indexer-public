package com.rarible.protocol.solana.nft.listener.block.cache

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rarible.blockchain.scanner.solana.client.dto.GetBlockRequest
import com.rarible.core.apm.withTransaction
import com.rarible.core.task.TaskHandler
import com.rarible.solana.block.SolanaBlockCompressingApi
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class BlockCacheTaskHandler(
    private val repository: BlockCacheRepository,
    private val blockCacheProperties: BlockCacheProperties,
    private val meterRegistry: MeterRegistry,
    @Qualifier("solanaBlockCompressingApi") private val solanaBlockCompressingApi: SolanaBlockCompressingApi
) : TaskHandler<Long> {

    private val mapper = jacksonObjectMapper()

    private val blockCacheSaveTimer by lazy { meterRegistry.timer("block_cache_save") }

    private val blockCacheLoadTimer by lazy { meterRegistry.timer("block_cache_load") }

    override val type: String = "BLOCK_CACHE"

    override fun runLongTask(from: Long?, param: String): Flow<Long> {
        return getRangesReversed(param.toLong(), from!!, blockCacheProperties.batchSize).asFlow()
            .map {
                logger.info("processing range {}", it)
                withTransaction("cacheBlocks", labels = listOf("blocks" to it.toString())) {
                    loadRange(it)
                }
                it.minOf { v -> v }
            }
    }

    private suspend fun loadRange(blocks: LongRange) {
        coroutineScope {
            val loadStartTime = Timer.start()
            val loadedBlocks = blocks.map {
                async {
                    if (!repository.isPresent(it)) {
                        logger.info("BlockCacheTaskHandler: loading block $it")
                        val block = solanaBlockCompressingApi.getBlock(it, GetBlockRequest.TransactionDetails.Full)
                        if (block.result != null) {
                            it to mapper.writeValueAsBytes(block)
                        } else {
                            logger.info("BlockCacheTaskHandler: block $it is missing")
                            null
                        }
                    } else {
                        logger.info("block cache already exists: $it")
                        null
                    }
                }
            }.awaitAll().filterNotNull().toMap()
            logger.info("Loaded ${blocks.count()} blocks in ${loadStartTime.stop(blockCacheLoadTimer).nanoToMillis()}")

            val saveStartTime = Timer.start()
            repository.save(loadedBlocks)
            logger.info("Saved ${blocks.count()} blocks in ${saveStartTime.stop(blockCacheSaveTimer).nanoToMillis()}")
        }
    }

    private fun Long.nanoToMillis() = TimeUnit.NANOSECONDS.toMillis(this)

    companion object {
        val logger: Logger = LoggerFactory.getLogger(BlockCacheTaskHandler::class.java)
    }
}

fun getRangesReversed(from: Long, to: Long, step: Int): Sequence<LongRange> {
    check(from >= 0) { "$from " }
    check(to >= 0) { "$to" }
    if (from > to) return emptySequence()
    return (from..to).reversed().asSequence().chunked(step) { list ->
        LongRange(list.minOf { it }, list.maxOf { it })
    }
}
