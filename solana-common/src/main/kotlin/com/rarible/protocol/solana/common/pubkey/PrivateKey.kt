package com.rarible.protocol.solana.common.pubkey

data class PrivateKey(
    val bytes: ByteArray
) {
    override fun equals(other: Any?): Boolean =
        other is PrivateKey && bytes.contentEquals(other.bytes)

    override fun hashCode(): Int = bytes.contentHashCode()
}
