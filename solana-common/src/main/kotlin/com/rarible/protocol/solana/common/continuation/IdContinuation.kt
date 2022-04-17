package com.rarible.protocol.solana.common.continuation

data class IdContinuation(
    val id: String,
    val asc: Boolean = true
) : Continuation<IdContinuation> {

    override fun toString(): String = id

    override fun compareTo(other: IdContinuation): Int =
        if (asc) {
            this.id.compareTo(other.id)
        } else {
            other.id.compareTo(this.id)
        }

}
