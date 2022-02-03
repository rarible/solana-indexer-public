package com.rarible.protocol.solana.nft.listener

import com.rarible.core.kafka.RaribleKafkaConsumer
import com.rarible.solana.protocol.dto.BalanceEventDto
import com.rarible.solana.protocol.dto.TokenEventDto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors

abstract class EventAwareBlockScannerTest : AbstractBlockScannerTest() {
    @Autowired
    private lateinit var tokenEventConsumer: RaribleKafkaConsumer<TokenEventDto>

    @Autowired
    private lateinit var balanceEventConsumer: RaribleKafkaConsumer<BalanceEventDto>

    protected val tokenEvents = CopyOnWriteArrayList<TokenEventDto>()
    protected val balanceEvents = CopyOnWriteArrayList<BalanceEventDto>()

    private val eventsConsumingScope = CoroutineScope(
        SupervisorJob() +
                Executors.newSingleThreadExecutor { Thread(it).apply { isDaemon = true } }.asCoroutineDispatcher()
    )

    @BeforeEach
    fun setUpEventConsumers() {
        eventsConsumingScope.launch {
            tokenEventConsumer.receiveAutoAck().collect { tokenEvents += it.value }
        }
        eventsConsumingScope.launch {
            balanceEventConsumer.receiveAutoAck().collect { balanceEvents += it.value }
        }
    }

    @AfterEach
    fun stopConsumers() {
        eventsConsumingScope.cancel()
    }
}
