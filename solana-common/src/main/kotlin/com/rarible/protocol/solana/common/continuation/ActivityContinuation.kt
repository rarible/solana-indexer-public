package com.rarible.protocol.solana.common.continuation

import com.rarible.protocol.solana.dto.ActivityDto

object ActivityContinuation {

    object ByLastUpdatedAndIdDesc :
        ContinuationFactory<ActivityDto, DateIdContinuation> {

        override fun getContinuation(entity: ActivityDto): DateIdContinuation {
            return DateIdContinuation(
                entity.date,
                entity.id,
                false
            )
        }
    }

    object ByLastUpdatedAndIdAsc :
        ContinuationFactory<ActivityDto, DateIdContinuation> {

        override fun getContinuation(entity: ActivityDto): DateIdContinuation {
            return DateIdContinuation(
                entity.date,
                entity.id,
                true
            )
        }
    }
}