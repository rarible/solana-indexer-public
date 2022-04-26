package com.rarible.protocol.solana.common.pubkey

import com.rarible.protocol.solana.common.util.TweetNaclFast
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.security.MessageDigest

/**
 * Program Derived Address (PDA) in Solana is a deterministic account controlled by a program and not a private key.
 *
 * https://docs.solana.com/developing/programming-model/calling-between-programs#program-derived-addresses
 * https://pencilflip.medium.com/learning-solana-3-what-is-a-program-derived-address-732b06def7c1
 */
data class ProgramDerivedAddress(val address: PublicKey, val nonce: Int)

object ProgramDerivedAddressCalc {
    /**
     * Metadata account associated with the [mint].
     */
    fun getMetadataAccount(mint: PublicKey): ProgramDerivedAddress {
        return findProgramAddress(
            listOf(
                "metadata".toByteArray(),
                PublicKey(SolanaProgramId.TOKEN_METADATA_PROGRAM).toByteArray(),
                mint.toByteArray()
            ),
            PublicKey(SolanaProgramId.TOKEN_METADATA_PROGRAM)
        )
    }

    /**
     * Associated Token Account for [mint] of the [owner].
     */
    fun getAssociatedTokenAccount(
        mint: PublicKey,
        owner: PublicKey
    ): ProgramDerivedAddress {
        return findProgramAddress(
            listOf(
                owner.toByteArray(),
                PublicKey(SolanaProgramId.SPL_TOKEN_PROGRAM).toByteArray(),
                mint.toByteArray()
            ),
            PublicKey(SolanaProgramId.ASSOCIATED_TOKEN_ACCOUNT_PROGRAM)
        )
    }

    fun findProgramAddress(
        seeds: List<ByteArray>,
        programId: PublicKey
    ): ProgramDerivedAddress {
        var nonce = 255
        val allSeeds = arrayListOf<ByteArray>()
        allSeeds += seeds
        while (nonce != 0) {
            val address = try {
                allSeeds += byteArrayOf(nonce.toByte())
                createProgramAddress(allSeeds, programId)
            } catch (e: Exception) {
                allSeeds.removeLast()
                nonce--
                continue
            }
            return ProgramDerivedAddress(address, nonce)
        }
        throw Exception("Unable to find PDA for $programId and seeds ${seeds.joinToString { it.contentToString() }}")
    }

    private fun createProgramAddress(
        seeds: List<ByteArray>,
        programId: PublicKey
    ): PublicKey {
        val buffer = ByteArrayOutputStream()
        val sha256 = MessageDigest.getInstance("SHA-256")

        for (seed in seeds) {
            require(seed.size <= 32) { "Max seed length exceeded" }
            try {
                buffer.write(seed)
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }
        try {
            buffer.write(programId.toByteArray())
            buffer.write("ProgramDerivedAddress".toByteArray())
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        val hash = sha256.digest(buffer.toByteArray())
        if (TweetNaclFast.is_on_curve(hash) != 0) {
            throw RuntimeException("Invalid seeds, address must fall off the curve")
        }
        return PublicKey(hash)
    }
}
