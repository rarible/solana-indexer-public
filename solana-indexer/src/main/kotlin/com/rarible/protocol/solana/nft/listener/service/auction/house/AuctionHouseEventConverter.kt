package com.rarible.protocol.solana.nft.listener.service.auction.house

import com.rarible.protocol.solana.common.event.AuctionHouseEvent
import com.rarible.protocol.solana.common.event.CreateAuctionHouseEvent
import com.rarible.protocol.solana.common.event.UpdateAuctionHouseEvent
import com.rarible.protocol.solana.common.records.SolanaAuctionHouseRecord
import com.rarible.protocol.solana.common.records.SolanaAuctionHouseRecord.CreateAuctionHouseRecord
import com.rarible.protocol.solana.common.records.SolanaAuctionHouseRecord.UpdateAuctionHouseRecord
import org.springframework.stereotype.Component

@Component
class AuctionHouseEventConverter {
    suspend fun convert(
        record: SolanaAuctionHouseRecord,
        reversed: Boolean
    ): List<AuctionHouseEvent> = when (record) {
        is CreateAuctionHouseRecord -> if (record.requiresSignOff != null && record.sellerFeeBasisPoints != null) {
            listOf(
                CreateAuctionHouseEvent(
                    sellerFeeBasisPoints = record.sellerFeeBasisPoints!!,
                    requiresSignOff = record.requiresSignOff!!,
                    log = record.log,
                    auctionHouse = record.auctionHouse,
                    timestamp = record.timestamp,
                    reversed = reversed
                )
            )
        } else {
            emptyList()
        }
        is UpdateAuctionHouseRecord ->
            listOf(
                UpdateAuctionHouseEvent(
                    sellerFeeBasisPoints = record.sellerFeeBasisPoints,
                    requiresSignOff = record.requiresSignOff,
                    log = record.log,
                    auctionHouse = record.auctionHouse,
                    timestamp = record.timestamp,
                    reversed = reversed
                )
            )
    }
}