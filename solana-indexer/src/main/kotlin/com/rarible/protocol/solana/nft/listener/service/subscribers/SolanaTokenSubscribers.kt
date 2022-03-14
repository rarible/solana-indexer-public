package com.rarible.protocol.solana.nft.listener.service.subscribers

import com.rarible.blockchain.scanner.solana.client.SolanaBlockchainBlock
import com.rarible.blockchain.scanner.solana.client.SolanaBlockchainLog
import com.rarible.blockchain.scanner.solana.model.SolanaDescriptor
import com.rarible.blockchain.scanner.solana.subscriber.SolanaLogEventSubscriber
import com.rarible.protocol.solana.borsh.Burn
import com.rarible.protocol.solana.borsh.BurnChecked
import com.rarible.protocol.solana.borsh.CreateAuctionHouse
import com.rarible.protocol.solana.borsh.InitializeMint1and2
import com.rarible.protocol.solana.borsh.MintTo
import com.rarible.protocol.solana.borsh.MintToChecked
import com.rarible.protocol.solana.borsh.UpdateAuctionHouse
import com.rarible.protocol.solana.borsh.parseAuctionHouseInstruction
import com.rarible.protocol.solana.borsh.parseTokenInstruction
import com.rarible.protocol.solana.common.util.toBigInteger
import com.rarible.protocol.solana.nft.listener.service.records.SolanaAuctionHouseRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaTokenRecord
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class InitializeMintSubscriber : SolanaLogEventSubscriber {
    override fun getDescriptor(): SolanaDescriptor = object : SolanaDescriptor(
        programId = SolanaProgramId.SPL_TOKEN_PROGRAM,
        id = "token_initialize_mint",
        groupId = SubscriberGroup.TOKEN.id,
        entityType = SolanaTokenRecord.InitializeMintRecord::class.java,
        collection = SubscriberGroup.TOKEN.collectionName
    ) {}

    override suspend fun getEventRecords(
        block: SolanaBlockchainBlock,
        log: SolanaBlockchainLog
    ): List<SolanaTokenRecord.InitializeMintRecord> {
        val initializeMint1and2Instruction = log.instruction.data.parseTokenInstruction() as? InitializeMint1and2
            ?: return emptyList()
        val record = SolanaTokenRecord.InitializeMintRecord(
            mint = log.instruction.accounts[0],
            mintAuthority = initializeMint1and2Instruction.mintAuthority,
            decimals = initializeMint1and2Instruction.decimal.toInt(),
            log = log.log,
            timestamp = Instant.ofEpochSecond(block.timestamp)
        )
        return listOf(record)
    }
}

@Component
class MintToTokenSubscriber : SolanaLogEventSubscriber {
    override fun getDescriptor(): SolanaDescriptor = object : SolanaDescriptor(
        programId = SolanaProgramId.SPL_TOKEN_PROGRAM,
        id = "token_mint_to",
        groupId = SubscriberGroup.TOKEN.id,
        entityType = SolanaTokenRecord.MintToRecord::class.java,
        collection = SubscriberGroup.TOKEN.collectionName
    ) {}

    override suspend fun getEventRecords(
        block: SolanaBlockchainBlock,
        log: SolanaBlockchainLog
    ): List<SolanaTokenRecord> {
        val record = when (val instruction = log.instruction.data.parseTokenInstruction()) {
            is MintTo -> SolanaTokenRecord.MintToRecord(
                mint = log.instruction.accounts[0],
                tokenAccount = log.instruction.accounts[1],
                mintAmount = instruction.amount.toBigInteger(),
                log = log.log,
                timestamp = Instant.ofEpochSecond(block.timestamp)
            )
            is MintToChecked -> SolanaTokenRecord.MintToRecord(
                mint = log.instruction.accounts[0],
                tokenAccount = log.instruction.accounts[1],
                mintAmount = instruction.amount.toBigInteger(),
                log = log.log,
                timestamp = Instant.ofEpochSecond(block.timestamp)
            )
            else -> return emptyList()
        }

        return listOf(record)
    }
}

@Component
class BurnTokenSubscriber : SolanaLogEventSubscriber {
    override fun getDescriptor(): SolanaDescriptor = object : SolanaDescriptor(
        programId = SolanaProgramId.SPL_TOKEN_PROGRAM,
        id = "token_burn",
        groupId = SubscriberGroup.TOKEN.id,
        entityType = SolanaTokenRecord.BurnRecord::class.java,
        collection = SubscriberGroup.TOKEN.collectionName
    ) {}

    override suspend fun getEventRecords(
        block: SolanaBlockchainBlock,
        log: SolanaBlockchainLog
    ): List<SolanaTokenRecord> {
        val record = when (val instruction = log.instruction.data.parseTokenInstruction()) {
            is Burn -> SolanaTokenRecord.BurnRecord(
                tokenAccount = log.instruction.accounts[0],
                mint = log.instruction.accounts[1],
                burnAmount = instruction.amount.toBigInteger(),
                log = log.log,
                timestamp = Instant.ofEpochSecond(block.timestamp)
            )
            is BurnChecked -> SolanaTokenRecord.BurnRecord(
                tokenAccount = log.instruction.accounts[0],
                mint = log.instruction.accounts[1],
                burnAmount = instruction.amount.toBigInteger(),
                log = log.log,
                timestamp = Instant.ofEpochSecond(block.timestamp)
            )
            else -> return emptyList()
        }

        return listOf(record)
    }
}

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