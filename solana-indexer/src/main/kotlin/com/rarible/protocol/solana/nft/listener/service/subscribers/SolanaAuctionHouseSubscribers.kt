package com.rarible.protocol.solana.nft.listener.service.subscribers

import com.rarible.blockchain.scanner.solana.client.SolanaBlockchainBlock
import com.rarible.blockchain.scanner.solana.client.SolanaBlockchainLog
import com.rarible.blockchain.scanner.solana.model.SolanaDescriptor
import com.rarible.blockchain.scanner.solana.subscriber.SolanaLogEventSubscriber
import com.rarible.protocol.solana.borsh.CreateAuctionHouse
import com.rarible.protocol.solana.borsh.UpdateAuctionHouse
import com.rarible.protocol.solana.borsh.parseAuctionHouseInstruction
import com.rarible.protocol.solana.common.records.SolanaAuctionHouseRecord
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class CreateAuctionHouseSubscriber : SolanaLogEventSubscriber {
    override fun getDescriptor(): SolanaDescriptor = object : SolanaDescriptor(
        programId = SolanaProgramId.AUCTION_HOUSE_PROGRAM,
        id = "create_auction_house",
        groupId = SubscriberGroup.AUCTION_HOUSE.id,
        entityType = SolanaAuctionHouseRecord.CreateAuctionHouseRecord::class.java,
        collection = SubscriberGroup.AUCTION_HOUSE.collectionName
    ) {}

    override suspend fun getEventRecords(
        block: SolanaBlockchainBlock,
        log: SolanaBlockchainLog
    ): List<SolanaAuctionHouseRecord> {
        val record = when (log.instruction.data.parseAuctionHouseInstruction(log.instruction.accounts.size)) {
            is CreateAuctionHouse -> SolanaAuctionHouseRecord.CreateAuctionHouseRecord(
                treasuryMint = log.instruction.accounts[0],
                feeWithdrawalDestination = log.instruction.accounts[3],
                treasuryWithdrawalDestination = log.instruction.accounts[4],
                auctionHouse = log.instruction.accounts[6],
                log = log.log,
                timestamp = Instant.ofEpochSecond(block.timestamp)
            )
            else -> return emptyList()
        }

        return listOf(record)
    }
}

@Component
class UpdateAuctionHouseSubscriber : SolanaLogEventSubscriber {
    override fun getDescriptor(): SolanaDescriptor = object : SolanaDescriptor(
        programId = SolanaProgramId.AUCTION_HOUSE_PROGRAM,
        id = "update_auction_house",
        groupId = SubscriberGroup.AUCTION_HOUSE.id,
        entityType = SolanaAuctionHouseRecord.UpdateAuctionHouseRecord::class.java,
        collection = SubscriberGroup.AUCTION_HOUSE.collectionName
    ) {}

    override suspend fun getEventRecords(
        block: SolanaBlockchainBlock,
        log: SolanaBlockchainLog
    ): List<SolanaAuctionHouseRecord> {
        val record = when (log.instruction.data.parseAuctionHouseInstruction(log.instruction.accounts.size)) {
            is UpdateAuctionHouse -> SolanaAuctionHouseRecord.UpdateAuctionHouseRecord(
                // TODO: what to do with changed fees?
                updatedTreasuryMint = log.instruction.accounts[0],
                updatedFeeWithdrawalDestination = log.instruction.accounts[3],
                updatedTreasuryWithdrawalDestination = log.instruction.accounts[4],
                auctionHouse = log.instruction.accounts[6],
                log = log.log,
                timestamp = Instant.ofEpochSecond(block.timestamp)
            )
            else -> return emptyList()
        }

        return listOf(record)
    }
}