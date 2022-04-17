package com.rarible.protocol.solana.common.continuation

import com.rarible.protocol.solana.dto.ActivityDto

object ActivityContinuation {

    class ById(val asc: Boolean) : ContinuationFactory<ActivityDto, IdContinuation> {
        override fun getContinuation(entity: ActivityDto): IdContinuation =
            IdContinuation(entity.id, asc)
    }

}