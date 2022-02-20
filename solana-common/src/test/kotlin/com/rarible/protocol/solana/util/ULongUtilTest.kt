package com.rarible.protocol.solana.util

import com.rarible.protocol.solana.common.util.toBigInteger
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigInteger

class ULongUtilTest {
    @Test
    fun `ulong to big integer`() {
        assertThat(0UL.toBigInteger()).isEqualTo(BigInteger.ZERO)
        assertThat(1UL.toBigInteger()).isEqualTo(BigInteger.ONE)
        assertThat(Long.MAX_VALUE.toULong().toBigInteger()).isEqualTo(BigInteger.valueOf(Long.MAX_VALUE))
        assertThat(Long.MAX_VALUE.toULong().plus(1UL).toBigInteger()).isEqualTo(BigInteger.valueOf(Long.MAX_VALUE).plus(BigInteger.ONE))
        assertThat(ULong.MAX_VALUE.toBigInteger()).isEqualTo(BigInteger.valueOf(Long.MAX_VALUE) + BigInteger.valueOf(Long.MAX_VALUE) + BigInteger.ONE)
    }
}
