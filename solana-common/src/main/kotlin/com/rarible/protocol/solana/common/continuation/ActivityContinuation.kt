package com.rarible.protocol.solana.common.continuation

import com.rarible.protocol.solana.dto.ActivityDto

object ActivityContinuation {

    class ById(val asc: Boolean) : ContinuationFactory<ActivityDto, IdContinuation> {
        override fun getContinuation(entity: ActivityDto): IdContinuation =
            IdContinuation(entity.id, asc)
    }

    class ByDbUpdatedAndId(val asc: Boolean) : ContinuationFactory<ActivityDto, DateIdContinuation> {
        override fun getContinuation(entity: ActivityDto): DateIdContinuation {
            return DateIdContinuation(entity.dbUpdatedAt ?: entity.date, entity.id, asc)
        }
    }

}