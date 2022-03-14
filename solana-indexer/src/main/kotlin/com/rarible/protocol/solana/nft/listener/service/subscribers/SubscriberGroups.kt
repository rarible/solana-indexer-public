package com.rarible.protocol.solana.nft.listener.service.subscribers

enum class SubscriberGroup {
    TOKEN,
    BALANCE,
    METAPLEX_META,
    AUCTION_HOUSE;

    val id: String get() = name.lowercase()

    val collectionName: String get() = "records-$id"
}
