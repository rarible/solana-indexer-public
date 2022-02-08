package com.rarible.protocol.solana.nft.listener.service.token

import com.rarible.core.entity.reducer.service.Reducer
import com.rarible.protocol.solana.common.event.BurnEvent
import com.rarible.protocol.solana.common.event.MetaplexCreateMetadataEvent
import com.rarible.protocol.solana.common.event.InitializeMintEvent
import com.rarible.protocol.solana.common.event.MintEvent
import com.rarible.protocol.solana.common.event.TokenEvent
import com.rarible.protocol.solana.common.event.TransferEvent
import com.rarible.protocol.solana.common.model.Token
import org.springframework.stereotype.Component

@Component
class ForwardValueTokenReducer : Reducer<TokenEvent, Token> {
    override suspend fun reduce(entity: Token, event: TokenEvent): Token {
        return when (event) {
            is MintEvent -> entity.copy(
                supply = entity.supply + event.amount,
                updatedAt = entity.updatedAt
            )
            is BurnEvent -> entity.copy(
                supply = entity.supply - event.amount,
                updatedAt = entity.updatedAt
            )
            is TransferEvent -> entity.copy(
                updatedAt = entity.updatedAt
            )
            is InitializeMintEvent -> entity.copy(
                createdAt = event.timestamp,
                updatedAt = entity.updatedAt
            )
            is MetaplexCreateMetadataEvent -> entity
        }
    }
}
