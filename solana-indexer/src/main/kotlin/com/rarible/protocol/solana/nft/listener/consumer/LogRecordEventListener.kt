package com.rarible.protocol.solana.nft.listener.consumer

import com.rarible.protocol.solana.common.records.SubscriberGroup

interface LogRecordEventListener {
    val id: String

    val subscriberGroup: SubscriberGroup

    suspend fun onEntityEvents(events: List<SolanaLogRecordEvent>)
}

object LogRecordEventListenerId {
    fun metaHistoryListenerId(env: String): String =
        "${prefix(env)}.meta.history.listener"

    fun tokenHistoryListenerId(env: String): String =
        "${prefix(env)}.token.history.listener"

    fun balanceHistoryListenerId(env: String): String =
        "${prefix(env)}.balance.history.listener"

    fun auctionHouseListenerId(env: String): String =
        "${prefix(env)}.auction.house.listener"

    fun auctionHousePropertiesListenerId(env: String): String =
        "${prefix(env)}.auction.house.properties.listener"

    fun escrowListenerId(env: String): String =
        "${prefix(env)}.escrow.listener"

    private fun prefix(env: String): String = "$env.protocol.solana.nft"
}
