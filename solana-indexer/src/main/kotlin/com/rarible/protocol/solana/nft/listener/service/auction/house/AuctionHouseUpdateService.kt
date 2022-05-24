package com.rarible.protocol.solana.nft.listener.service.auction.house

import com.rarible.core.entity.reducer.service.EntityService
import com.rarible.protocol.solana.common.filter.auctionHouse.SolanaAuctionHouseFilter
import com.rarible.protocol.solana.common.model.AuctionHouse
import com.rarible.protocol.solana.common.model.AuctionHouseId
import com.rarible.protocol.solana.common.model.isEmpty
import com.rarible.protocol.solana.common.repository.AuctionHouseRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class AuctionHouseUpdateService(
    private val auctionHouseRepository: AuctionHouseRepository,
    private val auctionHouseFilter: SolanaAuctionHouseFilter
) : EntityService<AuctionHouseId, AuctionHouse> {
    override suspend fun get(id: AuctionHouseId): AuctionHouse? {
        return auctionHouseRepository.findByAccount(id)
    }

    override suspend fun update(entity: AuctionHouse): AuctionHouse {
        if (entity.isEmpty) {
            logger.info("AuctionHouse is empty, skipping it: {}", entity.account)
            return entity
        }
        if (!auctionHouseFilter.isAcceptableForUpdateAuctionHouse(entity.account)) {
            logger.info("AuctionHouse update is ignored because auction house ${entity.account} is filtered out")
            return entity
        }
        val auctionHouse = auctionHouseRepository.save(entity)

        logger.info("Updated AuctionHouse: $entity")
        return auctionHouse
    }

    companion object {
        private val logger = LoggerFactory.getLogger(AuctionHouseUpdateService::class.java)
    }
}