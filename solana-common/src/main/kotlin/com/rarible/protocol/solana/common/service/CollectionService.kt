package com.rarible.protocol.solana.common.service

import com.rarible.protocol.solana.common.model.MetaplexOffChainMeta
import com.rarible.protocol.solana.common.model.SolanaCollection
import com.rarible.protocol.solana.common.model.SolanaCollectionV1
import com.rarible.protocol.solana.common.model.SolanaCollectionV2
import com.rarible.protocol.solana.common.repository.CollectionRepository
import kotlinx.coroutines.flow.toList
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class CollectionService(
    private val collectionRepository: CollectionRepository
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    suspend fun save(collection: SolanaCollection): SolanaCollection {
        return collectionRepository.save(collection)
    }

    suspend fun findById(id: String): SolanaCollection? {
        return collectionRepository.findById(id)
    }

    suspend fun findAll(fromId: String?, limit: Int): List<SolanaCollection> {
        return collectionRepository.findAll(fromId, limit).toList()
    }

    suspend fun updateCollectionV1(
        offChainMeta: MetaplexOffChainMeta
    ): SolanaCollectionV1? {
        val collection = offChainMeta.metaFields.collection ?: return null
        val exist = findById(collection.hash)
        if (exist != null) {
            return null
        }

        logger.info("Saved SolanaCollection V1: {}", collection)
        return save(
            SolanaCollectionV1(
                id = collection.hash,
                name = collection.name,
                family = collection.family
            )
        ) as SolanaCollectionV1
    }

    suspend fun updateCollectionV2(collectionAddress: String): SolanaCollectionV2? {
        val exist = findById(collectionAddress)
        if (exist != null) {
            return null
        }
        logger.info("Saved SolanaCollection V2: {}", collectionAddress)
        return save(SolanaCollectionV2(collectionAddress)) as SolanaCollectionV2
    }
}
