package com.rarible.protocol.solana.common.pubkey

import com.rarible.protocol.solana.borsh.Base58


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

        fun isPubKey(maybePubKey: String): Boolean {
            // TODO: Also add verification according to https://docs.solana.com/integrations/exchange#valid-ed25519-pubkey-check
            return try {
                Base58.decode(maybePubKey).size == PUBLIC_KEY_LENGTH
            } catch (e: Exception) {
                return false
            }
        }
    }
}
