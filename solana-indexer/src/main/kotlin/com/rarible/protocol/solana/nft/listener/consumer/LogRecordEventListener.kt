package com.rarible.protocol.solana.nft.listener.consumer

import com.rarible.protocol.solana.nft.listener.service.subscribers.SubscriberGroup

interface LogRecordEventListener {
    val id: String

    val subscriberGroup: SubscriberGroup

    suspend fun onEntityEvents(events: List<SolanaLogRecordEvent>)
}
