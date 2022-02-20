package com.rarible.protocol.solana.common.util

import java.math.BigInteger

fun ULong.toBigInteger(): BigInteger {
    val long = toLong()
    if (long >= 0) {
        return BigInteger.valueOf(long)
    }
    return BigInteger.valueOf(Long.MAX_VALUE) + BigInteger.valueOf(long - Long.MIN_VALUE) + BigInteger.ONE
}
