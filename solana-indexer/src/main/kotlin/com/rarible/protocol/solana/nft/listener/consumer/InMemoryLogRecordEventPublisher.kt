package com.rarible.protocol.solana.nft.listener.consumer

import com.rarible.blockchain.scanner.framework.data.LogRecordEvent
import com.rarible.blockchain.scanner.publisher.LogRecordEventPublisher
import com.rarible.protocol.solana.common.configuration.FeatureFlags
import com.rarible.protocol.solana.nft.listener.service.records.SolanaBaseLogRecord
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
@ConditionalOnProperty(name = ["common.featureFlags.enableInMemoryLogRecordHandling"], havingValue = "true")
class InMemoryLogRecordEventPublisher(
    logRecordEventListeners: List<LogRecordEventListener>,
    private val featureFlags: FeatureFlags
) : LogRecordEventPublisher {

    private val logger = LoggerFactory.getLogger(javaClass)

    private val listenersByGroup = logRecordEventListeners.associateBy { it.subscriberGroup.id }

    @PostConstruct
    fun postConstruct() {
        logger.info("Using InMemoryLogRecordEventPublisher instead of KafkaRecordEventPublisher")
    }

    override suspend fun publish(groupId: String, logRecordEvents: List<LogRecordEvent<*>>) {
        if (featureFlags.skipInMemoryLogRecordHandling) {
            // Let's skip event handling in order to reduce them later
            return
        }

        val listener = listenersByGroup[groupId]
        if (listener == null) {
            logger.error("Can't find log LogRecordEventListener with groupId={}", groupId)
            return
        }
        val records = logRecordEvents.map {
            SolanaLogRecordEvent(it.record as SolanaBaseLogRecord, it.reverted)
        }
        listener.onEntityEvents(records)
    }

}