package com.rarible.protocol.solana.common.update

import com.rarible.core.kafka.RaribleKafkaProducer
import com.rarible.protocol.solana.dto.CollectionDto
import com.rarible.protocol.solana.dto.CollectionEventDto
import com.rarible.protocol.solana.dto.CollectionUpdateEventDto
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.*

@Component
class CollectionUpdateListener(
    private val publisher: RaribleKafkaProducer<CollectionEventDto>
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    suspend fun onCollectionChanged(collection: CollectionDto) {
        val event = CollectionUpdateEventDto(
            eventId = UUID.randomUUID().toString(),
            collectionId = collection.address,
            collection = collection
        )
        val message = KafkaEventFactory.collectionEvent(event)
        publisher.send(message).ensureSuccess()
        logger.info("Collection event sent: $event")
    }
}