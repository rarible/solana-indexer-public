package com.rarible.protocol.solana.common.records

import com.rarible.blockchain.scanner.solana.model.SolanaLog
import java.time.Instant

sealed class SolanaAuctionHouseRecord : SolanaBaseLogRecord() {
    abstract val auctionHouse: String

    data class CreateAuctionHouseRecord(
        val sellerFeeBasisPoints: Int?,
        val requiresSignOff: Boolean?,
        val treasuryMint: String,
        val feeWithdrawalDestination: String,
        val treasuryWithdrawalDestination: String,
        override val log: SolanaLog,
        override val timestamp: Instant,
        override val auctionHouse: String
    ) : SolanaAuctionHouseRecord() {
        override fun getKey(): String = auctionHouse
    }

    data class UpdateAuctionHouseRecord(
        val sellerFeeBasisPoints: Int?,
        val requiresSignOff: Boolean?,
        val updatedTreasuryMint: String,
        val updatedFeeWithdrawalDestination: String,
        val updatedTreasuryWithdrawalDestination: String,
        override val log: SolanaLog,
        override val timestamp: Instant,
        override val auctionHouse: String
    ) : SolanaAuctionHouseRecord() {
        override fun getKey(): String = auctionHouse
    }

}
