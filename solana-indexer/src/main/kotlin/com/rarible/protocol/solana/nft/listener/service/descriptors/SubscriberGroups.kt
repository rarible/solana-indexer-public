package com.rarible.protocol.solana.nft.listener.service.descriptors

enum class SubscriberGroup {
    TOKEN,
    BALANCE,
    METAPLEX_META;

    val id: String get() = name.lowercase()

    val collectionName: String get() = "records-$id"
}
