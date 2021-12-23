package com.rarible.protocol.solana.nft.listener.consumer

interface EntityEventConsumer {
    fun start(handler: Map<SubscriberGroup, EntityEventListener>)
}