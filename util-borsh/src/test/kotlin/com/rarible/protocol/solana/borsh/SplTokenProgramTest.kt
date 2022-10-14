package com.rarible.protocol.solana.borsh

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*

class SplTokenProgramTest {
    @Test
    fun testInitializeMint() {
        val data = "AQAAAP//////////Ms05CQAAAAAHAYRa2oXdXYrZ3QFil6sI2yMo2AsbLb2slz9V+1dnxE3yAACUuG2lhKgYbsdBDZKT5URvd76aS1JO7rksNQu1WTYotc4BDmCv7bInF71jGS9UFFo/llozu4LSxwKess4eIIJkfOfqHfXaT1eI09Zzeq/xUMOEu2KWe4FXBoX8LQqLZjrm/P63cvPScw+MuWw93S7tgfcneWuEe9wHpVvmyT0dhaLzsNiTEAgZWOuH4TPkLaCwHBrnX1rjcMGa7CmzQFL+xvp6877brTo9ZfNqq8l0MbG75MLS9uDkfKYCA0UvXWGyp3GW5H+Zi7hgwfUaF9cagq82Hh3iFYlr4tKKLhEkJouD/iWzGv0LjNpT5rFuIfBqnJh4l1FfjAJ/e8SYSItv"
        val base64 = Base64.getDecoder().decode(data)
//        val bytes = Base58.decode(data)
        val buffer = ByteBuffer.wrap(base64).apply { order(ByteOrder.LITTLE_ENDIAN) }
        (1..56).forEach { buffer.get() }
        println(buffer.readPubkey())
        println(buffer.readPubkey())
        println(buffer.readPubkey())
        println(buffer.readPubkey())

//        assertThat(data.parseTokenInstruction()).isEqualTo(
//            InitializeMint1and2(
//                mintAuthority = "DC2mkgwhy56w3viNtHDjJQmc7SGu2QX785bS4aexojwX",
//                freezeAuthority = "DC2mkgwhy56w3viNtHDjJQmc7SGu2QX785bS4aexojwX",
//                decimal = 0.toUByte()
//            )
//        )
    }

    @Test
    fun testMintTo() {
        val data = "6AuM4xMCPFhR"
        assertThat(data.parseTokenInstruction()).isEqualTo(
            MintTo(amount = 1UL)
        )
    }

    @Test
    fun testInitializeAccount() {
        val data = "2"
        assertThat(data.parseTokenInstruction()).isEqualTo(InitializeAccount)
    }

    @Test
    fun testTransfer() {
        val data = "3DbEuZHcyqBD"
        assertThat(data.parseTokenInstruction()).isEqualTo(Transfer(amount = 1_000_000_000UL))
    }

    @Test
    fun testBurn() {
        val data = "72hHu8EXU5VZ"
        assertThat(data.parseTokenInstruction()).isEqualTo(Burn(amount = 135_901_226UL))
    }
}
