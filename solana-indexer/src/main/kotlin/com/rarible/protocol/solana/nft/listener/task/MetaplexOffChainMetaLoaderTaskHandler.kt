package com.rarible.protocol.solana.nft.listener.task

import com.rarible.core.task.TaskHandler
import com.rarible.protocol.solana.common.meta.MetaplexOffChainMetaLoadService
import com.rarible.protocol.solana.common.repository.MetaplexMetaRepository
import com.rarible.protocol.solana.common.repository.MetaplexOffChainMetaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Background task used to load metaplex meta for items that have Metaplex on-chain meta but for reason lack the off-chain meta.
 */
@Component
class MetaplexOffChainMetaLoaderTaskHandler(
    private val metaplexMetaRepository: MetaplexMetaRepository,
    private val metaplexOffChainMetaRepository: MetaplexOffChainMetaRepository,
    private val metaplexOffChainMetaLoadService: MetaplexOffChainMetaLoadService
) : TaskHandler<String> {

    private val logger = LoggerFactory.getLogger(MetaplexOffChainMetaLoaderTaskHandler::class.java)

    override val type: String = "METAPLEX_META_LOADER"

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
            "Starting to load metaplex off-chain meta" +
                    (if (from != null) " from $from" else "") +
                    (if (toMint != null) " to $toMint" else "") +
                    " with reloading = $reload"
        )
        return metaplexMetaRepository.findAll(from, toMint)
            .map {
                val tokenAddress = it.tokenAddress
                val hasOffChainMeta = metaplexOffChainMetaRepository.findByTokenAddress(tokenAddress) != null
                if (reload || !hasOffChainMeta) {
                    logger.info(
                        "Loading off chain metaplex meta for $tokenAddress" +
                                if (reload) "because reload is requested" else " because the off-chain meta has not been loaded before"
                    )
                    metaplexOffChainMetaLoadService.loadOffChainTokenMeta(tokenAddress)
                }
                tokenAddress
            }
    }

}