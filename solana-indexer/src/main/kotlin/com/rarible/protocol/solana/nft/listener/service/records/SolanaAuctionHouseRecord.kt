package com.rarible.protocol.solana.nft.listener.service.records

import com.rarible.blockchain.scanner.solana.model.SolanaLog
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
        val treasuryMint: String,
        val feeWithdrawalDestination: String,
        val treasuryWithdrawalDestination: String,
        override val log: SolanaLog,
        override val timestamp: Instant,
        override val auctionHouse: String
    ) : SolanaAuctionHouseRecord()
}