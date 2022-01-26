package com.rarible.protocol.solana.nft.listener.consumer

import com.rarible.blockchain.scanner.configuration.KafkaProperties
import com.rarible.blockchain.scanner.util.getLogTopicPrefix
import com.rarible.core.daemon.RetryProperties
import com.rarible.core.daemon.sequential.ConsumerEventHandler
import com.rarible.core.daemon.sequential.ConsumerWorker
import com.rarible.core.daemon.sequential.ConsumerWorkerHolder
import com.rarible.core.kafka.RaribleKafkaConsumer
import com.rarible.core.kafka.json.JsonDeserializer
import com.rarible.protocol.solana.nft.listener.service.records.SolanaItemLogRecord
import io.micrometer.core.instrument.MeterRegistry
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import java.time.Duration

data class SolanaLogRecordEvent(
    val record: SolanaItemLogRecord,
    val reversed: Boolean
)

class KafkaEntityEventConsumer(
    private val properties: KafkaProperties,
    private val meterRegistry: MeterRegistry,
    host: String,
    environment: String,
    private val service: String
) : AutoCloseable {

    private val blockchain = "solana"
    private val topicPrefix = getLogTopicPrefix(environment, service, blockchain)
    private val clientIdPrefix = "$environment.$host.${java.util.UUID.randomUUID()}.$blockchain"
    private val batchedConsumerWorkers = arrayListOf<ConsumerWorkerHolder<*>>()

    fun start(entityEventListeners: List<EntityEventListener>) {
        batchedConsumerWorkers += entityEventListeners
            .map { consumer(it) }
            .onEach { consumer -> consumer.start() }
    }

    override fun close() {
        batchedConsumerWorkers.forEach { consumer -> consumer.close() }
    }

    @Suppress("UNCHECKED_CAST")
    private fun consumer(listener: EntityEventListener): ConsumerWorkerHolder<SolanaLogRecordEvent> {
        val workers = (1..properties.numberOfPartitionsPerLogGroup).map { index ->
            val consumerGroup = listener.id
            val kafkaConsumer = RaribleKafkaConsumer(
                clientId = "$clientIdPrefix.log-event-consumer.$service.$consumerGroup-$index",
                valueDeserializerClass = JsonDeserializer::class.java,
                valueClass = SolanaLogRecordEvent::class.java,
                consumerGroup = consumerGroup,
                defaultTopic = "$topicPrefix.${listener.subscriberGroup}",
                bootstrapServers = properties.brokerReplicaSet,
                offsetResetStrategy = OffsetResetStrategy.EARLIEST,
                autoCreateTopic = false
            )

            ConsumerWorker(
                consumer = kafkaConsumer,
                // Block consumer should NOT skip events, so there is we're using endless retry
                retryProperties = RetryProperties(attempts = Integer.MAX_VALUE, delay = Duration.ofMillis(1000)),
                eventHandler = BlockEventHandler(listener),
                meterRegistry = meterRegistry,
                workerName = "log-event-consumer-${listener.id}-$index"
            )
        }
        return ConsumerWorkerHolder(workers)
    }

    private class BlockEventHandler(
        private val entityEventListener: EntityEventListener
    ) : ConsumerEventHandler<SolanaLogRecordEvent> {
        override suspend fun handle(event: SolanaLogRecordEvent) {
            entityEventListener.onEntityEvents(
                listOf(event)
            )
        }
    }
}