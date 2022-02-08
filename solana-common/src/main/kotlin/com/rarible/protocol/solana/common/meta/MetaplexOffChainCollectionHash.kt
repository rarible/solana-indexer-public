package com.rarible.protocol.solana.common.meta

import com.rarible.protocol.solana.common.hash.Hash.keccak256

object MetaplexOffChainCollectionHash {
    fun calculateCollectionHash(
        name: String,
        family: String,
        creators: List<String>
    ): String = keccak256(
        keccak256(name) + keccak256(family) + creators.joinToString { keccak256(it) }
    )
}
