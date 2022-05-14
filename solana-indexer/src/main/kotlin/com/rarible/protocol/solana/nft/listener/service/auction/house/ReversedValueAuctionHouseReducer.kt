package com.rarible.protocol.solana.nft.listener.service.auction.house

import com.rarible.core.entity.reducer.service.Reducer
import com.rarible.protocol.solana.common.event.AuctionHouseEvent
import com.rarible.protocol.solana.common.event.CreateAuctionHouseEvent
import com.rarible.protocol.solana.common.event.UpdateAuctionHouseEvent
import com.rarible.protocol.solana.common.model.AuctionHouse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class ReversedValueAuctionHouseReducer : Reducer<AuctionHouseEvent, AuctionHouse> {
    private val logger = LoggerFactory.getLogger(ReversedValueAuctionHouseReducer::class.java)

    override suspend fun reduce(entity: AuctionHouse, event: AuctionHouseEvent): AuctionHouse {
        val revertableEvents = entity.revertableEvents

        if (revertableEvents.isEmpty() || revertableEvents.last().log != event.log) {
            logger.error(
                "Revertable event error: attempt to revert an event which is not the latest one, event: $event, entity: $entity"
            )
            return entity
        }

        return when (event) {
            is CreateAuctionHouseEvent -> AuctionHouse.empty(event.auctionHouse)
            is UpdateAuctionHouseEvent -> {
                val events = revertableEvents.dropLast(1).asReversed()
                val requiresSignOff = events.firstNotNullOf {
                    when (it) {
                        is CreateAuctionHouseEvent -> it.requiresSignOff
                        is UpdateAuctionHouseEvent -> it.requiresSignOff
                    }
                }
                val sellerFeeBasisPoints = events.firstNotNullOf {
                    when (it) {
                        is CreateAuctionHouseEvent -> it.sellerFeeBasisPoints
                        is UpdateAuctionHouseEvent -> it.sellerFeeBasisPoints
                    }
                }

                entity.copy(
                    requiresSignOff = requiresSignOff,
                    sellerFeeBasisPoints = sellerFeeBasisPoints
                )
            }
        }
    }
}
