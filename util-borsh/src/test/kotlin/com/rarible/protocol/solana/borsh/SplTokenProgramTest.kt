package com.rarible.protocol.solana.borsh

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class SplTokenProgramTest {
    @Test
    fun testInitializeMint() {
        val data = "11Gz3BHfDcfCv418yvTCZKQKkEKDeiUBXvsyeYf2CL7mMiKECSf3jrp6fucwFz9qpMKxTNRVe4p3R21g8mHhMuU5h2R"
        val tokenInstruction = data.parseTokenInstruction() as InitializeMint

        assertEquals(tokenInstruction.mintAuthority, "DC2mkgwhy56w3viNtHDjJQmc7SGu2QX785bS4aexojwX")
        assertEquals(tokenInstruction.freezeAuthority, "DC2mkgwhy56w3viNtHDjJQmc7SGu2QX785bS4aexojwX")
        assertEquals(tokenInstruction.decimal, 0.toUByte())
    }

    @Test
    fun testMintTo() {
        val data = "6AuM4xMCPFhR"
        val tokenInstruction = data.parseTokenInstruction() as MintTo

        assertEquals(tokenInstruction.amount, 1UL)
    }

    @Test
    fun testInitializeAccount() {
        val data = "2"
        val tokenInstruction = data.parseTokenInstruction()

        assertTrue(tokenInstruction is InitializeAccount)
    }

    @Test
    fun testTransfer() {
        val data = "3DbEuZHcyqBD"
        val tokenInstruction = data.parseTokenInstruction() as Transfer

        assertEquals(tokenInstruction.amount, 1_000_000_000UL)
    }

    @Test
    fun testBurn() {
        val data = "72hHu8EXU5VZ"
        val tokenInstruction = data.parseTokenInstruction() as Burn

        assertEquals(tokenInstruction.amount, 135_901_226UL)
    }
}