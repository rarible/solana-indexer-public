package com.rarible.protocol.solana.common.pubkey

import org.bitcoinj.core.Base58

data class PublicKey(private val pubKey: ByteArray) {
    init {
        require(pubKey.size <= PUBLIC_KEY_LENGTH) { "Invalid public key input" }
    }

    constructor(pubKeyString: String) : this(Base58.decode(pubKeyString))

    fun toByteArray(): ByteArray = pubKey

    fun toBase58(): String = Base58.encode(pubKey)

    override fun equals(other: Any?): Boolean = other is PublicKey && pubKey.contentEquals(other.pubKey)

    override fun hashCode(): Int = pubKey.contentHashCode()

    override fun toString(): String = toBase58()

    companion object {
        const val PUBLIC_KEY_LENGTH = 32
    }
}
