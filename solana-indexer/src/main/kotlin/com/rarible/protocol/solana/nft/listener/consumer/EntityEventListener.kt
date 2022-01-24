package com.rarible.protocol.solana.nft.listener.consumer

import com.rarible.blockchain.scanner.framework.data.LogRecordEvent
import com.rarible.protocol.solana.nft.listener.service.records.SolanaItemLogRecord

typealias SubscriberGroup = String

interface EntityEventListener {
    val id: String

    val subscriberGroup: SubscriberGroup

    suspend fun onEntityEvents(events: List<LogRecordEvent<SolanaItemLogRecord>>)
}