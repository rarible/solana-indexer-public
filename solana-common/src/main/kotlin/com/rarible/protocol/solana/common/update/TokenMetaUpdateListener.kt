package com.rarible.protocol.solana.common.update

import com.rarible.core.kafka.RaribleKafkaProducer
import com.rarible.protocol.solana.common.converter.TokenMetaEventConverter
import com.rarible.protocol.solana.dto.TokenMetaEventDto
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class TokenMetaUpdateListener(
    private val publisher: RaribleKafkaProducer<TokenMetaEventDto>
) {
    private val logger = LoggerFactory.getLogger(TokenMetaUpdateListener::class.java)

    /**
     * Produce a trigger event that will make the Union-Service load the off-chain meta
     * for this token by calling /items/<SOLANA:itemId>/meta
     */
    suspend fun triggerTokenMetaLoading(tokenAddress: String) {
        logger.info("Trigger token meta $tokenAddress loading on the Union Service")
        send(TokenMetaEventConverter.convertTriggerEvent(tokenAddress))
    }

    private suspend fun send(tokenMetaEventDto: TokenMetaEventDto) {
        publisher.send(KafkaEventFactory.tokenMetaEvent(tokenMetaEventDto)).ensureSuccess()
        logger.info("TokenMeta event sent: $tokenMetaEventDto")
    }
}
