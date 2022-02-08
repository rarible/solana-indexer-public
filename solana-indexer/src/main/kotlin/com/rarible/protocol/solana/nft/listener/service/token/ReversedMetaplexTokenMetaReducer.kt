package com.rarible.protocol.solana.nft.listener.service.token

import com.rarible.core.entity.reducer.service.Reducer
import com.rarible.protocol.solana.common.event.BurnEvent
import com.rarible.protocol.solana.common.event.MetaplexCreateMetadataEvent
import com.rarible.protocol.solana.common.event.InitializeMintEvent
import com.rarible.protocol.solana.common.event.MintEvent
import com.rarible.protocol.solana.common.event.TokenEvent
import com.rarible.protocol.solana.common.event.TransferEvent
import com.rarible.protocol.solana.common.model.Token
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class ReversedMetaplexTokenMetaReducer : Reducer<TokenEvent, Token> {

    private val logger = LoggerFactory.getLogger(ReversedMetaplexTokenMetaReducer::class.java)

    override suspend fun reduce(entity: Token, event: TokenEvent): Token {
        return when (event) {
            is MetaplexCreateMetadataEvent -> {
                val meta = event.metadata
                val lastMeta = entity.metaplexMetaHistory.lastOrNull()
                if (meta == lastMeta) {
                    val revertedMetaHistory = entity.metaplexMetaHistory.dropLast(1)
                    val revertedMeta = revertedMetaHistory.lastOrNull()
                    entity.copy(metaplexMeta = revertedMeta, metaplexMetaHistory = revertedMetaHistory)
                } else {
                    // Generally, should not happen, we can remove this check if everything is ok.
                    logger.error("Last known meta state $lastMeta is not equal to the revertible meta $meta")
                    entity
                }
            }
            is BurnEvent,
            is InitializeMintEvent,
            is MintEvent,
            is TransferEvent -> entity
        }
    }
}
