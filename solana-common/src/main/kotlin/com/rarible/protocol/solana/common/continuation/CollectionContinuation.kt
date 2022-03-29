package com.rarible.protocol.solana.common.continuation

import com.rarible.protocol.solana.dto.CollectionDto

object CollectionContinuation {

    object ById : ContinuationFactory<CollectionDto, IdContinuation> {

        override fun getContinuation(entity: CollectionDto): IdContinuation {
            return IdContinuation(entity.address)
        }
    }
}