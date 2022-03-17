package com.rarible.protocol.solana.nft.listener.service.records

import com.rarible.blockchain.scanner.solana.model.SolanaLog
import java.math.BigInteger
import java.time.Instant

sealed class SolanaAuctionHouseRecord : SolanaBaseLogRecord() {
    abstract val auctionHouse: String

    override fun getKey(): String = auctionHouse

    data class CreateAuctionHouseRecord(
        val treasuryMint: String,
        val feeWithdrawalDestination: String,
        val treasuryWithdrawalDestination: String,
        override val log: SolanaLog,
        override val timestamp: Instant,
        override val auctionHouse: String
    ) : SolanaAuctionHouseRecord()

    data class UpdateAuctionHouseRecord(
        val updatedTreasuryMint: String,
        val updatedFeeWithdrawalDestination: String,
        val updatedTreasuryWithdrawalDestination: String,
        override val log: SolanaLog,
        override val timestamp: Instant,
        override val auctionHouse: String
    ) : SolanaAuctionHouseRecord()

    data class SellRecord(
        val sellPrice: BigInteger,
        val mint: String,
        val amount: BigInteger,
        override val log: SolanaLog,
        override val timestamp: Instant,
        override val auctionHouse: String
    ) : SolanaAuctionHouseRecord()

    data class BuyRecord(
        val buyPrice: BigInteger,
        val mint: String,
        val amount: BigInteger,
        override val log: SolanaLog,
        override val timestamp: Instant,
        override val auctionHouse: String
    ) : SolanaAuctionHouseRecord()

    data class ExecuteSellRecord(
        val price: BigInteger,
        val mint: String,
        val amount: BigInteger,
        override val log: SolanaLog,
        override val timestamp: Instant,
        override val auctionHouse: String
    ) : SolanaAuctionHouseRecord()
}