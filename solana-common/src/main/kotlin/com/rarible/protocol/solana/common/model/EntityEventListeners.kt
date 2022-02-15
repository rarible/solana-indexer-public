package com.rarible.protocol.solana.common.model

object EntityEventListeners {
    fun metaHistoryListenerId(env: String): String = "${prefix(env)}.meta.history.listener"

    fun tokenHistoryListenerId(env: String): String = "${prefix(env)}.token.history.listener"

    fun balanceHistoryListenerId(env: String): String = "${prefix(env)}.balance.history.listener"

    private fun prefix(env: String): String = "$env.protocol.solana.nft"
}
