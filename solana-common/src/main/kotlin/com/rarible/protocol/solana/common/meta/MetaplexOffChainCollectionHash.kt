package com.rarible.protocol.solana.common.meta

import com.rarible.protocol.solana.common.hash.Hash.keccak256

object MetaplexOffChainCollectionHash {
    fun calculateCollectionHash(
        name: String,
        family: String?,
        creators: List<String>,
    ): String = keccak256(
        keccak256(name) + if (family != null) keccak256(family) else "" + creators.joinToString { keccak256(it) }
    )
}
