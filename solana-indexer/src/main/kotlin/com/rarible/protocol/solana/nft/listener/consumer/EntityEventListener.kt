package com.rarible.protocol.solana.nft.listener.consumer

import com.rarible.protocol.solana.nft.listener.service.descriptors.SubscriberGroup

interface EntityEventListener {
    val id: String

    val subscriberGroup: SubscriberGroup

    suspend fun onEntityEvents(events: List<SolanaLogRecordEvent>)
}
