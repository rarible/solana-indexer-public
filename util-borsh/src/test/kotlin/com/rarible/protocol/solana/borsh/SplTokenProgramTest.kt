package com.rarible.protocol.solana.borsh

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SplTokenProgramTest {
    @Test
    fun testInitializeMint() {
        val data = "11Gz3BHfDcfCv418yvTCZKQKkEKDeiUBXvsyeYf2CL7mMiKECSf3jrp6fucwFz9qpMKxTNRVe4p3R21g8mHhMuU5h2R"
        assertThat(data.parseTokenInstruction()).isEqualTo(
            InitializeMint1and2(
                mintAuthority = "DC2mkgwhy56w3viNtHDjJQmc7SGu2QX785bS4aexojwX",
                freezeAuthority = "DC2mkgwhy56w3viNtHDjJQmc7SGu2QX785bS4aexojwX",
                decimal = 0.toUByte()
            )
        )
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
