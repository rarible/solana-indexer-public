package com.rarible.protocol.solana.nft.listener.service.subscribers

import com.rarible.blockchain.scanner.solana.client.SolanaBlockchainBlock
import com.rarible.blockchain.scanner.solana.client.SolanaBlockchainLog
import com.rarible.blockchain.scanner.solana.model.SolanaDescriptor
import com.rarible.blockchain.scanner.solana.subscriber.SolanaLogEventSubscriber
import com.rarible.protocol.solana.borsh.Buy
import com.rarible.protocol.solana.borsh.CreateAuctionHouse
import com.rarible.protocol.solana.borsh.ExecuteSale
import com.rarible.protocol.solana.borsh.Sell
import com.rarible.protocol.solana.borsh.UpdateAuctionHouse
import com.rarible.protocol.solana.borsh.parseAuctionHouseInstruction
import com.rarible.protocol.solana.common.util.toBigInteger
import com.rarible.protocol.solana.common.records.SolanaAuctionHouseRecord
import com.rarible.protocol.solana.common.records.SolanaAuctionHouseRecord.BuyRecord
import com.rarible.protocol.solana.common.records.SolanaAuctionHouseRecord.ExecuteSaleRecord
import com.rarible.protocol.solana.common.records.SolanaAuctionHouseRecord.SellRecord
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
        val record = when (log.instruction.data.parseAuctionHouseInstruction()) {
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
        val record = when (log.instruction.data.parseAuctionHouseInstruction()) {
            is UpdateAuctionHouse -> SolanaAuctionHouseRecord.UpdateAuctionHouseRecord(
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

@Component
class SellAuctionHouseSubscriber : SolanaLogEventSubscriber {
    override fun getDescriptor(): SolanaDescriptor = object : SolanaDescriptor(
        programId = SolanaProgramId.AUCTION_HOUSE_PROGRAM,
        id = "sell_auction_house",
        groupId = SubscriberGroup.AUCTION_HOUSE.id,
        entityType = SellRecord::class.java,
        collection = SubscriberGroup.AUCTION_HOUSE.collectionName
    ) {}

    override suspend fun getEventRecords(
        block: SolanaBlockchainBlock,
        log: SolanaBlockchainLog
    ): List<SellRecord> {
        val record = when (val instruction = log.instruction.data.parseAuctionHouseInstruction()) {
            is Sell -> SellRecord(
                maker = log.instruction.accounts[0],
                sellPrice = instruction.price.toBigInteger(),
                mint = log.instruction.accounts[1],
                amount = instruction.size.toBigInteger(),
                auctionHouse = log.instruction.accounts[4],
                log = log.log,
                timestamp = Instant.ofEpochSecond(block.timestamp)
            )
            else -> return emptyList()
        }

        return listOf(record)
    }
}

@Component
class BuyAuctionHouseSubscriber : SolanaLogEventSubscriber {
    override fun getDescriptor(): SolanaDescriptor = object : SolanaDescriptor(
        programId = SolanaProgramId.AUCTION_HOUSE_PROGRAM,
        id = "update_auction_house",
        groupId = SubscriberGroup.AUCTION_HOUSE.id,
        entityType = BuyRecord::class.java,
        collection = SubscriberGroup.AUCTION_HOUSE.collectionName
    ) {}

    override suspend fun getEventRecords(
        block: SolanaBlockchainBlock,
        log: SolanaBlockchainLog
    ): List<BuyRecord> {
        val record = when (val instruction = log.instruction.data.parseAuctionHouseInstruction()) {
            is Buy -> BuyRecord(
                maker = log.instruction.accounts[0],
                buyPrice = instruction.price.toBigInteger(),
                mint = log.instruction.accounts[4],
                amount = instruction.size.toBigInteger(),
                auctionHouse = log.instruction.accounts[8],
                log = log.log,
                timestamp = Instant.ofEpochSecond(block.timestamp)
            )
            else -> return emptyList()
        }

        return listOf(record)
    }
}

@Component
class ExecuteSellAuctionHouseSubscriber : SolanaLogEventSubscriber {
    override fun getDescriptor(): SolanaDescriptor = object : SolanaDescriptor(
        programId = SolanaProgramId.AUCTION_HOUSE_PROGRAM,
        id = "update_auction_house",
        groupId = SubscriberGroup.AUCTION_HOUSE.id,
        entityType = ExecuteSaleRecord::class.java,
        collection = SubscriberGroup.AUCTION_HOUSE.collectionName
    ) {}

    override suspend fun getEventRecords(
        block: SolanaBlockchainBlock,
        log: SolanaBlockchainLog
    ): List<ExecuteSaleRecord> {
        val record = when (val instruction = log.instruction.data.parseAuctionHouseInstruction()) {
            is ExecuteSale -> ExecuteSaleRecord(
                buyer = log.instruction.accounts[0],
                seller = log.instruction.accounts[1],
                price = instruction.buyerPrice.toBigInteger(),
                mint = log.instruction.accounts[3],
                amount = instruction.size.toBigInteger(),
                auctionHouse = log.instruction.accounts[10],
                log = log.log,
                timestamp = Instant.ofEpochSecond(block.timestamp)
            )
            else -> return emptyList()
        }

        return listOf(record)
    }
}
