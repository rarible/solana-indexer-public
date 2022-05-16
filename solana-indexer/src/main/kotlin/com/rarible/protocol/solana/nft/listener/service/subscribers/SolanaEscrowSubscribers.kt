package com.rarible.protocol.solana.nft.listener.service.subscribers

import com.rarible.blockchain.scanner.solana.client.SolanaBlockchainBlock
import com.rarible.blockchain.scanner.solana.client.SolanaBlockchainLog
import com.rarible.blockchain.scanner.solana.model.SolanaDescriptor
import com.rarible.blockchain.scanner.solana.subscriber.SolanaLogEventSubscriber
import com.rarible.protocol.solana.borsh.Buy
import com.rarible.protocol.solana.borsh.Deposit
import com.rarible.protocol.solana.borsh.ExecuteSale
import com.rarible.protocol.solana.borsh.Withdraw
import com.rarible.protocol.solana.borsh.parseAuctionHouseInstruction
import com.rarible.protocol.solana.common.pubkey.SolanaProgramId
import com.rarible.protocol.solana.common.records.SolanaEscrowRecord.BuyRecord
import com.rarible.protocol.solana.common.records.SolanaEscrowRecord.DepositRecord
import com.rarible.protocol.solana.common.records.SolanaEscrowRecord.ExecuteSaleRecord
import com.rarible.protocol.solana.common.records.SolanaEscrowRecord.WithdrawRecord
import com.rarible.protocol.solana.common.records.SubscriberGroup
import com.rarible.protocol.solana.common.util.toBigInteger
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class EscrowDepositSubscriber : SolanaLogEventSubscriber {

    override fun getDescriptor(): SolanaDescriptor = object : SolanaDescriptor(
        programId = SolanaProgramId.AUCTION_HOUSE_PROGRAM,
        id = "escrow_deposit",
        groupId = SubscriberGroup.ESCROW.id,
        entityType = DepositRecord::class.java,
        collection = SubscriberGroup.ESCROW.collectionName
    ) {}

    override suspend fun getEventRecords(
        block: SolanaBlockchainBlock,
        log: SolanaBlockchainLog
    ): List<DepositRecord> {
        val instruction = log.instruction.data.parseAuctionHouseInstruction()
        if (instruction !is Deposit) return emptyList()

        return listOf(
            DepositRecord(
                wallet = log.instruction.accounts[0],
                escrow = log.instruction.accounts[3],
                amount = instruction.amount.toBigInteger(),
                log = log.log,
                auctionHouse = log.instruction.accounts[6],
                timestamp = Instant.ofEpochSecond(block.timestamp)
            )
        )
    }
}

@Component
class EscrowWithdrawSubscriber : SolanaLogEventSubscriber {

    override fun getDescriptor(): SolanaDescriptor = object : SolanaDescriptor(
        programId = SolanaProgramId.AUCTION_HOUSE_PROGRAM,
        id = "withdraw_deposit",
        groupId = SubscriberGroup.ESCROW.id,
        entityType = WithdrawRecord::class.java,
        collection = SubscriberGroup.ESCROW.collectionName
    ) {}

    override suspend fun getEventRecords(
        block: SolanaBlockchainBlock,
        log: SolanaBlockchainLog
    ): List<WithdrawRecord> {
        val instruction = log.instruction.data.parseAuctionHouseInstruction()
        if (instruction !is Withdraw) return emptyList()

        return listOf(
            WithdrawRecord(
                wallet = log.instruction.accounts[0],
                escrow = log.instruction.accounts[2],
                amount = instruction.amount.toBigInteger(),
                log = log.log,
                auctionHouse = log.instruction.accounts[5],
                timestamp = Instant.ofEpochSecond(block.timestamp)
            )
        )
    }
}

@Component
class EscrowBuySubscriber : SolanaLogEventSubscriber {

    override fun getDescriptor(): SolanaDescriptor = object : SolanaDescriptor(
        programId = SolanaProgramId.AUCTION_HOUSE_PROGRAM,
        id = "escrow_buy",
        groupId = SubscriberGroup.ESCROW.id,
        entityType = BuyRecord::class.java,
        collection = SubscriberGroup.ESCROW.collectionName
    ) {}

    override suspend fun getEventRecords(
        block: SolanaBlockchainBlock,
        log: SolanaBlockchainLog
    ): List<BuyRecord> {
        val instruction = log.instruction.data.parseAuctionHouseInstruction()
        if (instruction !is Buy) return emptyList()

        return listOf(
            BuyRecord(
                wallet = log.instruction.accounts[0],
                escrow = log.instruction.accounts[6],
                buyPrice = instruction.price.toBigInteger(),
                log = log.log,
                auctionHouse = log.instruction.accounts[8],
                timestamp = Instant.ofEpochSecond(block.timestamp)
            )
        )
    }
}

@Component
class EscrowExecuteSaleSubscriber : SolanaLogEventSubscriber {

    override fun getDescriptor(): SolanaDescriptor = object : SolanaDescriptor(
        programId = SolanaProgramId.AUCTION_HOUSE_PROGRAM,
        id = "escrow_execute_sale",
        groupId = SubscriberGroup.ESCROW.id,
        entityType = ExecuteSaleRecord::class.java,
        collection = SubscriberGroup.ESCROW.collectionName
    ) {}

    override suspend fun getEventRecords(
        block: SolanaBlockchainBlock,
        log: SolanaBlockchainLog
    ): List<ExecuteSaleRecord> {
        val instruction = log.instruction.data.parseAuctionHouseInstruction()
        if (instruction !is ExecuteSale) return emptyList()

        return listOf(
            ExecuteSaleRecord(
                escrow = log.instruction.accounts[6],
                wallet = log.instruction.accounts[0],
                buyPrice = instruction.buyerPrice.toBigInteger(),
                log = log.log,
                auctionHouse = log.instruction.accounts[10],
                timestamp = Instant.ofEpochSecond(block.timestamp)
            )
        )
    }
}