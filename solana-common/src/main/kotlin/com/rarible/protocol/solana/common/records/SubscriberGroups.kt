package com.rarible.protocol.solana.common.records

enum class SubscriberGroup {
    ESCROW,
    TOKEN,
    BALANCE,
    METAPLEX_META,
    AUCTION_HOUSE,
    AUCTION_HOUSE_ORDER;

    val id: String get() = name.lowercase()

    val collectionName: String get() = "records-$id"
}
