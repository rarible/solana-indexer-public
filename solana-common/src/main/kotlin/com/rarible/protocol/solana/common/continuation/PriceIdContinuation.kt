package com.rarible.protocol.solana.common.continuation

import java.math.BigDecimal

data class PriceIdContinuation(
    private val price: BigDecimal?,
    private val id: String,
    private val asc: Boolean = false
) : Continuation<PriceIdContinuation> {

    private val sign = if (asc) 1 else -1

    override fun compareTo(other: PriceIdContinuation): Int {

        // Then we compare prices inside same currency
        val result = compareNullable(this.price, other.price)
        if (result != 0) return result

        // Otherwise - using sorting by OrderId
        return this.id.compareTo(other.id) * sign
    }

    private fun compareNullable(thisPrice: BigDecimal?, otherPrice: BigDecimal?): Int {
        return if (thisPrice == null) {
            if (otherPrice == null) 0 else 1
        } else {
            if (otherPrice == null) -1 else thisPrice.compareTo(otherPrice) * sign
        }
    }

    override fun toString(): String {
        // In case if we have null prices (originally, we should not),
        // continuation should ignore price by using min/max value depends on sort
        val from = price ?: if (asc) "0" else Long.MAX_VALUE.toString()
        return "${from}_${id}"
    }

}
