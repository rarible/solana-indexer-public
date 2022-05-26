package com.rarible.protocol.solana.common.filter.auctionHouse

interface SolanaAuctionHouseFilter {

    fun isAcceptableAuctionHouse(auctionHouse: String): Boolean

}