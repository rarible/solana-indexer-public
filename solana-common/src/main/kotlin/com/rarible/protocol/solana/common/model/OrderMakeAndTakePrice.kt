package com.rarible.protocol.solana.common.model

import java.math.BigDecimal

data class OrderMakeAndTakePrice(
    val makePrice: BigDecimal?,
    val takePrice: BigDecimal?,
)