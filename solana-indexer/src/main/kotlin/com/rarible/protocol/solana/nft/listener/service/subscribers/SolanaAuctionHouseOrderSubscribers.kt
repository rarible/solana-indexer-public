package com.rarible.protocol.solana.nft.listener.service.subscribers

import com.rarible.blockchain.scanner.solana.client.SolanaBlockchainBlock
import com.rarible.blockchain.scanner.solana.client.SolanaBlockchainLog
import com.rarible.blockchain.scanner.solana.model.SolanaDescriptor
import com.rarible.blockchain.scanner.solana.subscriber.SolanaLogEventSubscriber
import com.rarible.protocol.solana.borsh.Buy
import com.rarible.protocol.solana.borsh.Cancel
import com.rarible.protocol.solana.borsh.ExecuteSale
import com.rarible.protocol.solana.borsh.Sell
import com.rarible.protocol.solana.borsh.parseAuctionHouseInstruction
import com.rarible.protocol.solana.common.pubkey.SolanaProgramId
import com.rarible.protocol.solana.common.records.OrderDirection
import com.rarible.protocol.solana.common.records.SolanaAuctionHouseOrderRecord
import com.rarible.protocol.solana.common.records.SubscriberGroup
import com.rarible.protocol.solana.nft.listener.util.transactionLogs
import org.springframework.stereotype.Component
import java.math.BigInteger

@Component
class AuctionHouseOrderSellSubscriber : SolanaLogEventSubscriber {

    override fun getDescriptor(): SolanaDescriptor = object : SolanaDescriptor(
        programId = SolanaProgramId.AUCTION_HOUSE_PROGRAM,
        id = "auction_house_order_sell",
        groupId = SubscriberGroup.AUCTION_HOUSE_ORDER.id,
        entityType = SolanaAuctionHouseOrderRecord.SellRecord::class.java,
        collection = SubscriberGroup.AUCTION_HOUSE_ORDER.collectionName
    ) {}

    override suspend fun getEventRecords(
        block: SolanaBlockchainBlock,
        log: SolanaBlockchainLog
    ): List<SolanaAuctionHouseOrderRecord.SellRecord> {
        when (val instruction = log.instruction.data.parseAuctionHouseInstruction()) {
            is Sell -> {
                val sellRecord = SolanaAuctionHouseLogConverter.convertSell(log, instruction, block.timestamp)

                if (sellRecord.amount == BigInteger.ZERO) return emptyList()
                // It means this is ad-hoc sell order, we don't need to save such record
                if (block.hasExecuteSell(log.log.transactionHash) { it.seller == sellRecord.maker }) return emptyList()

                return listOf(sellRecord)
            }
            else -> return emptyList()
        }
    }
}

@Component
class AuctionHouseOrderBuySubscriber : SolanaLogEventSubscriber {

    override fun getDescriptor(): SolanaDescriptor = object : SolanaDescriptor(
        programId = SolanaProgramId.AUCTION_HOUSE_PROGRAM,
        id = "auction_house_buy",
        groupId = SubscriberGroup.AUCTION_HOUSE_ORDER.id,
        entityType = SolanaAuctionHouseOrderRecord.BuyRecord::class.java,
        collection = SubscriberGroup.AUCTION_HOUSE_ORDER.collectionName
    ) {}

    override suspend fun getEventRecords(
        block: SolanaBlockchainBlock,
        log: SolanaBlockchainLog
    ): List<SolanaAuctionHouseOrderRecord.BuyRecord> {
        return when (val instruction = log.instruction.data.parseAuctionHouseInstruction()) {
            is Buy -> {
                val buyRecord = SolanaAuctionHouseLogConverter.convertBuy(log, instruction, block.timestamp)

                if (buyRecord.amount == BigInteger.ZERO) return emptyList()
                // It means this is ad-hoc buy order, we don't need to save such record
                if (block.hasExecuteSell(log.log.transactionHash) { it.buyer == buyRecord.maker }) return emptyList()

                return listOf(buyRecord)
            }
            else -> emptyList()
        }
    }
}

@Component
class AuctionHouseOrderExecuteSaleSubscriber : SolanaLogEventSubscriber {

    override fun getDescriptor(): SolanaDescriptor = object : SolanaDescriptor(
        programId = SolanaProgramId.AUCTION_HOUSE_PROGRAM,
        id = "auction_house_execute_sale",
        groupId = SubscriberGroup.AUCTION_HOUSE_ORDER.id,
        entityType = SolanaAuctionHouseOrderRecord.ExecuteSaleRecord::class.java,
        collection = SubscriberGroup.AUCTION_HOUSE_ORDER.collectionName
    ) {}

    override suspend fun getEventRecords(
        block: SolanaBlockchainBlock,
        log: SolanaBlockchainLog
    ): List<SolanaAuctionHouseOrderRecord.ExecuteSaleRecord> {
        return when (val instruction = log.instruction.data.parseAuctionHouseInstruction()) {
            is ExecuteSale -> {
                val executeSaleRecord = SolanaAuctionHouseLogConverter.convertExecuteSale(log, instruction, block.timestamp)

                if (executeSaleRecord.amount == BigInteger.ZERO) return emptyList()

                val result = ArrayList<SolanaAuctionHouseOrderRecord.ExecuteSaleRecord>(2)

                // Don't want to filter logs twice, so moved out this action here
                val filteredLogs = block.transactionLogs(log.log.transactionHash, SolanaProgramId.AUCTION_HOUSE_PROGRAM)

                // There is no add-hoc SELL order, it means previously created SELL order matched
                if (!block.hasSell(filteredLogs, executeSaleRecord.seller)) {
                    result.add(executeSaleRecord.copy(direction = OrderDirection.SELL))
                }
                // There is no add-hoc BUY order, it means previously created BUY order matched
                if (!block.hasBuy(filteredLogs, executeSaleRecord.buyer)) {
                    result.add(executeSaleRecord.copy(direction = OrderDirection.BUY))
                }
                result
            }
            else -> emptyList()
        }
    }
}

@Component
class AuctionHouseOrderCancelSubscriber : SolanaLogEventSubscriber {

    override fun getDescriptor(): SolanaDescriptor = object : SolanaDescriptor(
        programId = SolanaProgramId.AUCTION_HOUSE_PROGRAM,
        id = "auction_house_cancel",
        groupId = SubscriberGroup.AUCTION_HOUSE_ORDER.id,
        entityType = SolanaAuctionHouseOrderRecord.CancelRecord::class.java,
        collection = SubscriberGroup.AUCTION_HOUSE_ORDER.collectionName
    ) {}

    override suspend fun getEventRecords(
        block: SolanaBlockchainBlock,
        log: SolanaBlockchainLog
    ): List<SolanaAuctionHouseOrderRecord.CancelRecord> {
        return when (
            val instruction = log.instruction.data.parseAuctionHouseInstruction()
        ) {
            is Cancel -> {
                val cancelRecord = SolanaAuctionHouseLogConverter.convertCancel(log, instruction, block.timestamp)
                listOf(
                    cancelRecord.withUpdatedOrderId(),
                    cancelRecord.copy(direction = OrderDirection.SELL).withUpdatedOrderId()
                )
            }
            else -> return emptyList()
        }
    }
}

private fun SolanaBlockchainBlock.hasExecuteSell(
    transactionHash: String,
    matcher: (r: SolanaAuctionHouseOrderRecord.ExecuteSaleRecord) -> Boolean
): Boolean {
    return transactionLogs(transactionHash, SolanaProgramId.AUCTION_HOUSE_PROGRAM).any {
        val instruction = it.instruction.data.parseAuctionHouseInstruction()
        if (instruction is ExecuteSale) {
            val executeSale = SolanaAuctionHouseLogConverter.convertExecuteSale(it, instruction, timestamp)
            matcher(executeSale)
        } else {
            false
        }
    }
}

private fun SolanaBlockchainBlock.hasSell(
    filteredLogs: List<SolanaBlockchainLog>,
    seller: String
): Boolean {
    for (log in filteredLogs) {
        val instruction = log.instruction.data.parseAuctionHouseInstruction()
        if (instruction !is Sell) continue

        val sell = SolanaAuctionHouseLogConverter.convertSell(log, instruction, timestamp)
        if (sell.maker == seller) return true
    }
    return false
}

private fun SolanaBlockchainBlock.hasBuy(
    filteredLogs: List<SolanaBlockchainLog>,
    buyer: String
): Boolean {
    for (log in filteredLogs) {
        val instruction = log.instruction.data.parseAuctionHouseInstruction()
        if (instruction !is Buy) continue

        val buy = SolanaAuctionHouseLogConverter.convertBuy(log, instruction, timestamp)
        if (buy.maker == buyer) return true
    }
    return false
}