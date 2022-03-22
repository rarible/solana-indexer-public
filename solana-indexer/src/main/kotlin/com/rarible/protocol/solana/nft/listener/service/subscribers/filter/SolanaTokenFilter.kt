package com.rarible.protocol.solana.nft.listener.service.subscribers.filter

interface SolanaTokenFilter {

    fun isAcceptableToken(mint: String): Boolean

}