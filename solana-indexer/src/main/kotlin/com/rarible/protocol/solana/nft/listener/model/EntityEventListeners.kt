package com.rarible.protocol.solana.nft.listener.model

object EntityEventListeners {
    fun itemHistoryListenerId(env: String): String = "${prefix(env)}.item.history.listener"

    fun balanceHistoryListenerId(env: String): String = "${prefix(env)}.balance.history.listener"

    private fun prefix(env: String): String = "$env.protocol.solana.nft"
}