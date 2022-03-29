package com.rarible.protocol.solana.common.continuation

import com.rarible.protocol.solana.dto.TokenDto

object TokenContinuation {

    object ByLastUpdatedAndId : ContinuationFactory<TokenDto, DateIdContinuation> {

        override fun getContinuation(entity: TokenDto): DateIdContinuation {
            return DateIdContinuation(entity.updatedAt, entity.address)
        }
    }

    object ById : ContinuationFactory<TokenDto, IdContinuation> {

        override fun getContinuation(entity: TokenDto): IdContinuation {
            return IdContinuation(entity.address)
        }
    }
}