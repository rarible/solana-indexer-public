package com.rarible.protocol.solana.nft.listener.task

import com.rarible.core.task.TaskHandler
import com.rarible.protocol.solana.common.repository.MetaplexMetaRepository
import com.rarible.protocol.solana.common.repository.MetaplexOffChainMetaRepository
import com.rarible.protocol.solana.common.update.TokenMetaUpdateListener
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Background task used to trigger loading of meta on the union-service for mints.
 */
@Component
class MetaplexMetaLoadingTriggerTaskHandler(
    private val metaplexMetaRepository: MetaplexMetaRepository,
    private val metaplexOffChainMetaRepository: MetaplexOffChainMetaRepository,
    private val tokenMetaUpdateListener: TokenMetaUpdateListener
) : TaskHandler<String> {

    private val logger = LoggerFactory.getLogger(MetaplexMetaLoadingTriggerTaskHandler::class.java)

    override val type: String = "METAPLEX_META_LOADING_TRIGGER"

    /**
     * - [param] = "" - load all starting [from] if not loaded yet
     * - [param] = "reload" - reload all starting [from]
     * - [param] = `128nFwDjdEBNhWMnutLog1YwXGqkYzuBuZQhJBqjN7FC` - load all starting [from] to [param] if not loaded yet
     * - [param] = `128nFwDjdEBNhWMnutLog1YwXGqkYzuBuZQhJBqjN7FC:reload` - re-load all starting [from] to [param]
     */
    override fun runLongTask(from: String?, param: String): Flow<String> {
        val (toMint, reload) = when {
            param.isEmpty() -> null to false
            param.contains(":") -> param.substringBefore(":") to (param.substringAfter(":") == "reload")
            param == "reload" -> null to true
            else -> param to false
        }
        logger.info(
            "Triggering meta loading on the union-service for mints" +
                    (if (from != null) " from $from" else "") +
                    (if (toMint != null) " to $toMint" else "") +
                    " with reloading = $reload"
        )
        return metaplexMetaRepository.findAll(from, toMint)
            .map {
                val tokenAddress = it.tokenAddress
                val hasOffChainMeta = metaplexOffChainMetaRepository.hasByTokenAddress(tokenAddress)
                if (reload || !hasOffChainMeta) {
                    tokenMetaUpdateListener.triggerTokenMetaLoading(tokenAddress)
                }
                tokenAddress
            }
    }

}