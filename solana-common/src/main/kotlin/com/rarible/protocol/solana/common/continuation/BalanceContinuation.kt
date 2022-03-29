package com.rarible.protocol.solana.common.continuation

import com.rarible.protocol.solana.dto.BalanceDto

object BalanceContinuation {

    object ByLastUpdatedAndId : ContinuationFactory<BalanceDto, DateIdContinuation> {

        override fun getContinuation(entity: BalanceDto): DateIdContinuation {
            return DateIdContinuation(entity.updatedAt, entity.account)
        }
    }
}