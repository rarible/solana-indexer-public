package com.rarible.protocol.solana.common.meta

import com.rarible.protocol.solana.common.model.TokenId
import com.rarible.protocol.solana.common.repository.MetaplexMetaRepository
import com.rarible.protocol.solana.common.service.CollectionUpdateService
import com.rarible.protocol.solana.common.update.MetaplexMetaUpdateListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@Component
class MetaplexOffChainMetaLoadService(
    private val metaplexMetaRepository: MetaplexMetaRepository,
    private val metaplexOffChainMetaLoader: MetaplexOffChainMetaLoader,
    private val collectionUpdateService: CollectionUpdateService
) {

    @Lazy
    @Autowired
    private lateinit var metaplexMetaUpdateListener: MetaplexMetaUpdateListener

    suspend fun loadOffChainTokenMeta(tokenAddress: TokenId): TokenMeta? {
        val onChainMeta = metaplexMetaRepository.findByTokenAddress(tokenAddress) ?: return null
        val metaFields = onChainMeta.metaFields
        val metaplexOffChainMeta = metaplexOffChainMetaLoader.loadMetaplexOffChainMeta(
            tokenAddress = tokenAddress,
            metaplexMetaFields = metaFields
        ) ?: return null
        collectionUpdateService.updateCollectionV1AndSendUpdate(metaplexOffChainMeta)

        val tokenMeta = TokenMetaParser.mergeOnChainAndOffChainMeta(metaFields, metaplexOffChainMeta.metaFields)
        metaplexMetaUpdateListener.onTokenMetaChanged(tokenAddress, tokenMeta)
        return tokenMeta
    }

}
