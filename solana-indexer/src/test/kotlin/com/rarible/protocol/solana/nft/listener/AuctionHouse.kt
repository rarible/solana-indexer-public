package com.rarible.protocol.solana.nft.listener

data class AuctionHouse(
    val id: String,
    val mint: String,
    val authority: String,
    val creator: String,
    val feePayerAcct: String,
    val treasuryAcct: String,
    val feePayerWithdrawalAcct: String,
    val treasuryWithdrawalAcct: String,
    val sellerFeeBasisPoints: Int
)