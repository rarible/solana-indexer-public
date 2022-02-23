package com.rarible.protocol.solana.common.update

import com.rarible.core.kafka.RaribleKafkaProducer
import com.rarible.protocol.solana.common.converter.TokenWithMetaEventConverter
import com.rarible.protocol.solana.common.meta.TokenMetaService
import com.rarible.protocol.solana.common.model.Token
import com.rarible.solana.protocol.dto.TokenEventDto
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class TokenUpdateListener(
    private val publisher: RaribleKafkaProducer<TokenEventDto>,
    private val tokenWithMetaService: TokenMetaService
) {
    private val logger = LoggerFactory.getLogger(TokenUpdateListener::class.java)

    suspend fun onTokenChanged(token: Token) {
        // TODO: listen to changes of on-chain/off-chain meta and send token update events.
        val tokenWithMeta = tokenWithMetaService.extendWithAvailableMeta(token)
        val tokenEventDto = TokenWithMetaEventConverter.convert(tokenWithMeta)
        publisher.send(KafkaEventFactory.tokenEvent(tokenEventDto)).ensureSuccess()
        logger.info("Token event sent: $tokenEventDto")
    }
}
