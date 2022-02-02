package com.rarible.protocol.solana.nft.listener

import kotlin.math.pow

fun Int.scaleSupply(decimals: Int) = this * 10.0.pow(decimals.toDouble()).toLong()
