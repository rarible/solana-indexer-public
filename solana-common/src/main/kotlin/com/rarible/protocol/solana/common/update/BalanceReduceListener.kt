package com.rarible.protocol.solana.common.update

import com.rarible.core.kafka.RaribleKafkaProducer
import com.rarible.protocol.solana.common.converter.BalanceWithMetaEventConverter
import com.rarible.protocol.solana.common.meta.TokenMetaService
import com.rarible.protocol.solana.common.model.Balance
import com.rarible.solana.protocol.dto.BalanceEventDto
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class BalanceReduceListener(
    private val publisher: RaribleKafkaProducer<BalanceEventDto>,
    private val tokenWithMetaService: TokenMetaService
) {
    private val logger = LoggerFactory.getLogger(BalanceReduceListener::class.java)

    suspend fun onBalanceChanged(balance: Balance) {
        // TODO: listen to changes of on-chain/off-chain meta and send balance update events.
        val balanceWithMeta = tokenWithMetaService.extendWithAvailableMeta(balance)
        val balanceEventDto = BalanceWithMetaEventConverter.convert(balanceWithMeta)
        publisher.send(KafkaEventFactory.balanceEvent(balanceEventDto)).ensureSuccess()
        logger.info("Balance event sent: $balanceEventDto")
    }
}
