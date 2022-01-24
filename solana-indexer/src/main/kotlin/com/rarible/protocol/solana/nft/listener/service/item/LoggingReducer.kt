package com.rarible.protocol.solana.nft.listener.service.item

import com.rarible.blockchain.scanner.framework.data.LogRecordEvent
import com.rarible.core.entity.reducer.model.Entity
import com.rarible.core.entity.reducer.service.Reducer
import com.rarible.protocol.solana.nft.listener.service.records.SolanaItemLogRecord
import org.slf4j.LoggerFactory

class LoggingReducer<Id, Event : LogRecordEvent<SolanaItemLogRecord>, E : Entity<Id, Event, E>> : Reducer<Event, E> {
    override suspend fun reduce(entity: E, event: Event): E {
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
