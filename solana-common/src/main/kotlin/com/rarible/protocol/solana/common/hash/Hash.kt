package com.rarible.protocol.solana.common.hash

import org.bouncycastle.jcajce.provider.digest.Keccak

/**
 * Crypto related functions (keccak-256).
 */
object Hash {

    @Suppress("SpellCheckingInspection")
    private val hexArray = "0123456789abcdef".toCharArray()

    fun keccak256(str: String): String = keccak256(str.toByteArray(Charsets.US_ASCII))

    private fun keccak256(bytes: ByteArray): String {
        val keccak: Keccak.DigestKeccak = Keccak.Digest256()
        keccak.update(bytes, 0, bytes.size)
        return prefixed(keccak.digest())
    }

    private fun prefixed(bytes: ByteArray): String {
        val hexChars = CharArray(2 + bytes.size * 2)
        hexChars[0] = '0'
        hexChars[1] = 'x'
        putHex(hexChars, bytes, 2)
        return String(hexChars)
    }

    private fun putHex(
        hexChars: CharArray,
        bytes: ByteArray,
        @Suppress("SameParameterValue") start: Int
    ) {
        for (j in bytes.indices) {
            val v: Int = bytes[j].toInt() and 0xFF
            hexChars[start + j * 2] = hexArray[v ushr 4]
            hexChars[start + j * 2 + 1] = hexArray[v and 0x0F]
        }
    }
}
