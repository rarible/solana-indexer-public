package com.rarible.protocol.solana.nft.listener.service.item

import com.rarible.core.entity.reducer.model.Entity
import com.rarible.core.entity.reducer.service.Reducer
import com.rarible.protocol.solana.nft.listener.consumer.SolanaLogRecordEvent
import org.slf4j.LoggerFactory

class LoggingReducer<Id, E : Entity<Id, SolanaLogRecordEvent, E>> : Reducer<SolanaLogRecordEvent, E> {
    override suspend fun reduce(entity: E, event: SolanaLogRecordEvent): E {
        val log = event.record.log

        logger.info(
            "event: {}, block: {}, instructionIndex: {}, innerInstructionIndex: {}, id: {}",
            event::class.java.simpleName,
            log.blockNumber,
            log.instructionIndex,
            log.innerInstructionIndex,
            entity.id
        )
        return entity
    }

    companion object {
        private val logger = LoggerFactory.getLogger(LoggingReducer::class.java)
    }
}
