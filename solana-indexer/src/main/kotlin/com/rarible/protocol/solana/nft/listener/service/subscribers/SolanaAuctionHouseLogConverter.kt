package com.rarible.protocol.solana.nft.listener.service.subscribers

import com.rarible.blockchain.scanner.solana.client.SolanaBlockchainLog
import com.rarible.protocol.solana.borsh.Buy
import com.rarible.protocol.solana.borsh.Cancel
import com.rarible.protocol.solana.borsh.ExecuteSale
import com.rarible.protocol.solana.borsh.Sell
import com.rarible.protocol.solana.common.records.OrderDirection
import com.rarible.protocol.solana.common.records.SolanaAuctionHouseOrderRecord
import com.rarible.protocol.solana.common.util.toBigInteger
import java.time.Instant

object SolanaAuctionHouseLogConverter {

    fun convert(
        log: SolanaBlockchainLog, instruction: ExecuteSale, dateSeconds: Long
    ): SolanaAuctionHouseOrderRecord.ExecuteSaleRecord {
        return SolanaAuctionHouseOrderRecord.ExecuteSaleRecord(
            buyer = log.instruction.accounts[0],
            seller = log.instruction.accounts[1],
            price = instruction.buyerPrice.toBigInteger(),
            mint = log.instruction.accounts[3],
            treasuryMint = log.instruction.accounts[5],
            amount = instruction.size.toBigInteger(),
            auctionHouse = log.instruction.accounts[10],
            log = log.log,
            timestamp = Instant.ofEpochSecond(dateSeconds),
            direction = OrderDirection.SELL
        )
    }

    fun convert(
        log: SolanaBlockchainLog, instruction: Buy, dateSeconds: Long
    ): SolanaAuctionHouseOrderRecord.BuyRecord {
        return SolanaAuctionHouseOrderRecord.BuyRecord(
            maker = log.instruction.accounts[0],
            treasuryMint = log.instruction.accounts[3],
            buyPrice = instruction.price.toBigInteger(),
            // Only the token account is available in the record.
            // Mint will be set in the SolanaRecordsLogEventFilter by account <-> mint association.
            tokenAccount = log.instruction.accounts[4],
            mint = "",
            amount = instruction.size.toBigInteger(),
            auctionHouse = log.instruction.accounts[8],
            log = log.log,
            timestamp = Instant.ofEpochSecond(dateSeconds),
            orderId = ""
        )
    }

    fun convert(
        log: SolanaBlockchainLog, instruction: Sell, dateSeconds: Long
    ): SolanaAuctionHouseOrderRecord.SellRecord {
        return SolanaAuctionHouseOrderRecord.SellRecord(
            maker = log.instruction.accounts[0],
            sellPrice = instruction.price.toBigInteger(),
            // Only the token account is available in the record.
            // Mint will be set in the SolanaRecordsLogEventFilter by account <-> mint association.
            tokenAccount = log.instruction.accounts[1],
            mint = "",
            amount = instruction.size.toBigInteger(),
            auctionHouse = log.instruction.accounts[4],
            log = log.log,
            timestamp = Instant.ofEpochSecond(dateSeconds),
            orderId = ""
        )
    }

    fun convert(
        log: SolanaBlockchainLog, instruction: Cancel, dateSeconds: Long
    ): SolanaAuctionHouseOrderRecord.CancelRecord {
        return SolanaAuctionHouseOrderRecord.CancelRecord(
            maker = log.instruction.accounts[0],
            mint = log.instruction.accounts[2],
            price = instruction.price.toBigInteger(),
            amount = instruction.size.toBigInteger(),
            log = log.log,
            timestamp = Instant.ofEpochSecond(dateSeconds),
            auctionHouse = log.instruction.accounts[4],
            direction = OrderDirection.BUY,
            orderId = ""
        )
    }

}