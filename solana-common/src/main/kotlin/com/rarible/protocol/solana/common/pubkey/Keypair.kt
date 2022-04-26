package com.rarible.protocol.solana.common.pubkey

import com.rarible.protocol.solana.common.util.TweetNaclFast

data class Keypair(
    val publicKey: PublicKey,
    val privateKey: PrivateKey
) {
    companion object {
        fun createRandom(): Keypair {
            val keyPair = TweetNaclFast.Box.keyPair()
            return Keypair(
                PublicKey(keyPair.publicKey),
                PrivateKey(keyPair.secretKey)
            )
        }
    }
}