package com.rarible.protocol.solana.common.update

import com.rarible.core.kafka.RaribleKafkaProducer
import com.rarible.protocol.solana.common.converter.BalanceWithMetaConverter
import com.rarible.protocol.solana.common.converter.BalanceWithMetaEventConverter
import com.rarible.protocol.solana.common.event.BalanceChangeOwnerEvent
import com.rarible.protocol.solana.common.meta.TokenMetaService
import com.rarible.protocol.solana.common.model.Balance
import com.rarible.protocol.solana.common.model.BalanceWithMeta
import com.rarible.protocol.solana.dto.BalanceDeleteEventDto
import com.rarible.protocol.solana.dto.BalanceEventDto
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.*

@Component
class BalanceUpdateListener(
    private val publisher: RaribleKafkaProducer<BalanceEventDto>,
    private val tokenMetaService: TokenMetaService
) {
    private val logger = LoggerFactory.getLogger(BalanceUpdateListener::class.java)

    suspend fun onBalanceChanged(balance: Balance) {
        val balance = tokenMetaService.extendWithAvailableMeta(balance)
        onBalanceChanged(balance)
    }

    suspend fun onBalanceChanged(balanceWithMeta: BalanceWithMeta) {
        val balanceEventDto = BalanceWithMetaEventConverter.convert(balanceWithMeta)
        publisher.send(KafkaEventFactory.balanceEvent(balanceEventDto)).ensureSuccess()

        val balance = balanceWithMeta.balance
        if (balance.lastEvent is BalanceChangeOwnerEvent) {
            val deleteEventDto = BalanceDeleteEventDto(
                eventId = UUID.randomUUID().toString(),
                mint = balance.mint,
                account = balance.account,
                balance = BalanceWithMetaConverter.convert(
                    balanceWithMeta.copy(balance = balance.copy(owner = balance.lastEvent.oldOwner))
                )
            )

            publisher.send(KafkaEventFactory.balanceEvent(deleteEventDto)).ensureSuccess()
            logger.info("Balance event sent: $deleteEventDto")
        }

        logger.info("Balance event sent: $balanceEventDto")
    }
}
