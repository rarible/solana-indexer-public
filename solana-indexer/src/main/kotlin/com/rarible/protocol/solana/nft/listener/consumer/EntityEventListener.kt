package com.rarible.protocol.solana.nft.listener.consumer

import com.rarible.blockchain.scanner.framework.data.LogRecordEvent
import com.rarible.blockchain.scanner.solana.model.SolanaLogRecord

typealias SubscriberGroup = String

interface EntityEventListener {
    val groupId: SubscriberGroup

    suspend fun onEntityEvents(events: List<LogRecordEvent<SolanaLogRecord>>)
}