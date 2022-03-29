package com.rarible.protocol.solana.common.continuation

import java.time.Instant

data class DateIdContinuation(
    val date: Instant,
    val id: String,
    val asc: Boolean = false
) : Continuation<DateIdContinuation> {

    private val sign = if (asc) 1 else -1

    override fun toString(): String {
        return "${date.toEpochMilli()}_${id}"
    }

    override fun compareTo(other: DateIdContinuation): Int {
        val dateDiff = this.date.compareTo(other.date)
        if (dateDiff != 0) {
            return sign * dateDiff
        }
        return this.id.compareTo(other.id) * sign
    }

    companion object {

        fun parse(str: String?): DateIdContinuation? {
            val pair = Continuation.splitBy(str, "_") ?: return null
            val timestamp = pair.first
            val id = pair.second
            return DateIdContinuation(Instant.ofEpochMilli(timestamp.toLong()), id)
        }
    }

}
