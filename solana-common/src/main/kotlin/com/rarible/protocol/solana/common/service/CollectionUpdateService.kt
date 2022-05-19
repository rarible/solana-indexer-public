package com.rarible.protocol.solana.common.service

import com.rarible.protocol.solana.common.model.MetaplexOffChainMeta
import com.rarible.protocol.solana.common.model.SolanaCollection
import com.rarible.protocol.solana.common.model.SolanaCollectionV2
import com.rarible.protocol.solana.common.model.Token
import com.rarible.protocol.solana.common.update.CollectionUpdateListener
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class CollectionUpdateService(
    private val collectionService: CollectionService,
    private val collectionConverter: CollectionConverter,
    private val collectionUpdateListener: CollectionUpdateListener
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    suspend fun updateCollectionV1AndSendUpdate(metaplexOffChainMeta: MetaplexOffChainMeta) {
        val collection = collectionService.saveCollectionV1(metaplexOffChainMeta) ?: return
        sendUpdateEvent(collection)
    }

    suspend fun updateCollectionV2AndSendUpdate(collectionMint: String) {
        val collection = collectionService.saveCollectionV2(collectionMint) ?: return
        sendUpdateEvent(collection)
    }

    /**
     * Hint by SDK that a concrete NFT is actually a collection NFT because we cannot
     * distinguish individual NFTs from collection NFTs while the collection is empty.
     */
    suspend fun markNftAsCollection(collectionMint: String) {
        logger.info("Marking NFT $collectionMint as collection V2")
        updateCollectionV2AndSendUpdate(collectionMint)
    }

    /**
     * Some NFTs may be collection V2 NFTs. We need to send CollectionUpdateEvent-s for them.
     */
    suspend fun onTokenChanged(token: Token) {
        val isCollectionNft = collectionService.findById(token.mint) != null
        if (isCollectionNft) {
            sendUpdateEvent(SolanaCollectionV2(token.mint))
        }
    }

    private suspend fun sendUpdateEvent(collection: SolanaCollection) {
        val dto = collectionConverter.toDto(collection) ?: return
        collectionUpdateListener.onCollectionChanged(dto)
    }
}