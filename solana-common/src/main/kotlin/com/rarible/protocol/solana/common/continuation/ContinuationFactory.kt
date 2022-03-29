package com.rarible.protocol.solana.common.continuation

interface ContinuationFactory<T, C : Continuation<C>> {

    fun getContinuation(entity: T): C

}