package com.rarible.protocol.solana.borsh

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class Base58Test {
    @Test
    fun encode() {
        Assertions.assertEquals(
            "72k1xXWG59wUsYv7h2",
            Base58.encode("Hello, world!".toByteArray())
        )
    }

    @Test
    fun decode() {
        Assertions.assertArrayEquals(
            "The quick brown fox jumps over the lazy dog".toByteArray(),
            Base58.decode("7DdiPPYtxLjCD3wA1po2rvZHTDYjkZYiEtazrfiwJcwnKCizhGFhBGHeRdx")
        )
    }
}