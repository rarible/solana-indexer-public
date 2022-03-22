package com.rarible.protocol.solana.nft.listener.service.subscribers.filter

interface SolanaTokenFilter {

    companion object {

    }

    fun isAcceptableToken(mint: String): Boolean

}