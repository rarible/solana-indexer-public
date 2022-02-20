package com.rarible.protocol.solana.hash

import com.rarible.protocol.solana.common.hash.Hash
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class HashTest {
    @Test
    fun keccak256() {
        assertThat(Hash.keccak256("")).isEqualTo("0xc5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a470")
        assertThat(Hash.keccak256("Hello")).isEqualTo("0x06b3dfaec148fb1bb2b066f10ec285e7c9bf402ab32aa78a5d38e34566810cd2")
    }
}
