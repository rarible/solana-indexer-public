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

    override fun runLongTask(from: String?, param: String): Flow<String> {
        val reload = param == "reload"
        logger.info("Starting to load metaplex off-chain meta" + (if (from != null) " from $from" else "") + " with reloading = $reload")
        return metaplexMetaRepository.findAll(from)
            .map {
                val tokenAddress = it.tokenAddress
                if (reload || metaplexOffChainMetaRepository.findByTokenAddress(tokenAddress) == null) {
                    metaplexOffChainMetaLoadService.loadOffChainTokenMeta(tokenAddress)
                }
                tokenAddress
            }
    }

}