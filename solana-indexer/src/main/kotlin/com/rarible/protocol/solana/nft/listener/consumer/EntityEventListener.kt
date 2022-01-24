package com.rarible.protocol.solana.nft.listener.consumer

typealias SubscriberGroup = String

interface EntityEventListener {
    val id: String

    val subscriberGroup: SubscriberGroup

    suspend fun onEntityEvents(events: List<SolanaLogRecordEvent>)
}