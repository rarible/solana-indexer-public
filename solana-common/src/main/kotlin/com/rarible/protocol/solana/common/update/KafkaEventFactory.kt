package com.rarible.protocol.solana.common.update

import com.rarible.core.kafka.KafkaMessage
import com.rarible.protocol.solana.dto.BalanceEventDto
import com.rarible.protocol.solana.dto.SolanaEventTopicProvider
import com.rarible.protocol.solana.dto.TokenEventDto

object KafkaEventFactory {

    private val TOKEN_EVENT_HEADERS = mapOf(
        "protocol.solana.token.event.version" to SolanaEventTopicProvider.VERSION
    )
    private val BALANCE_EVENT_HEADERS = mapOf(
        "protocol.solana.balance.event.version" to SolanaEventTopicProvider.VERSION
    )

    fun tokenEvent(dto: TokenEventDto): KafkaMessage<TokenEventDto> =
        KafkaMessage(
            id = dto.eventId,
            key = dto.address,
            value = dto,
            headers = TOKEN_EVENT_HEADERS
        )

    fun balanceEvent(dto: BalanceEventDto): KafkaMessage<BalanceEventDto> =
        KafkaMessage(
            id = dto.eventId,
            key = dto.account,
            value = dto,
            headers = BALANCE_EVENT_HEADERS
        )

}
