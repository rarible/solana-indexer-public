package com.rarible.protocol.solana.nft.listener.block.cache

import com.rarible.blockchain.scanner.solana.client.dto.GetBlockRequest
import com.rarible.core.apm.withTransaction
import com.rarible.core.task.TaskHandler
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class BlockCacheTaskHandler(
    private val client: BlockCacheClient,
    private val repository: BlockCacheRepository,
    private val blockCacheProperties: BlockCacheProperties,
) : TaskHandler<Long> {
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
            val loadedBlocks = blocks.map {
                async {
                    if (!repository.isPresent(it)) {
                        logger.info("loading block $it")
                        val result = client.getBlock(it, GetBlockRequest.TransactionDetails.Full)
                        it to result
                    } else {
                        logger.info("block cache already exists: $it")
                        null
                    }
                }
            }.awaitAll().filterNotNull().toMap()
            repository.save(loadedBlocks)
        }
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(BlockCacheTaskHandler::class.java)
    }
}

private fun getRangesReversed(from: Long, to: Long, step: Int): Sequence<LongRange> {
    check(from >= 0) { "$from "}
    check(to >= 0) { "$to" }
    if (from > to) return emptySequence()
    return (from..to).reversed().asSequence().chunked(step) { list ->
        LongRange(list.minOf { it }, list.maxOf { it })
    }
}
