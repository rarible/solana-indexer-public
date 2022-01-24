package com.rarible.protocol.solana.nft.listener.model

object EntityEventListeners {
    fun itemHistoryListenerId(env: String): String = "${prefix(env)}.item.history.listener"

    private fun prefix(env: String): String = "$env.protocol.solana.nft"
}