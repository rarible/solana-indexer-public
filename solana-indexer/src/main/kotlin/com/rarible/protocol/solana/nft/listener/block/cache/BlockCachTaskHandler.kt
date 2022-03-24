package com.rarible.protocol.solana.nft.listener.block.cache

import com.rarible.blockchain.scanner.solana.client.dto.GetBlockRequest
import com.rarible.core.task.TaskHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class BlockCachTaskHandler(
    private val client: BlockCacheClient,
    private val repository: BlockCacheRepository,
) : TaskHandler<Long> {
    override val type: String = "BLOCK_CACHE"

    override fun runLongTask(from: Long?, param: String): Flow<Long> {
        val to = param.toLong()
        return ((from ?: 0L) .. to).asFlow()
            .map {
                if (!repository.isPresent(it)) {
                    logger.info("loading block $it")
                    val result = client.getBlock(it, GetBlockRequest.TransactionDetails.Full)
                    repository.save(it, result)
                } else {
                    logger.info("block cache already exists: $it")
                }
                it
            }
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(BlockCachTaskHandler::class.java)
    }
}
