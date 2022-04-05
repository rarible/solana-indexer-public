package com.rarible.protocol.solana.nft.listener.configuration

import com.rarible.core.application.ApplicationEnvironmentInfo
import com.rarible.core.daemon.DaemonWorkerProperties
import com.rarible.core.daemon.sequential.ConsumerBatchWorker
import com.rarible.core.daemon.sequential.ConsumerWorkerHolder
import com.rarible.protocol.solana.dto.BalanceEventDto
import com.rarible.protocol.solana.nft.listener.service.order.balance.OrderBalanceConsumerEventHandler
import com.rarible.protocol.solana.subscriber.SolanaEventsConsumerFactory
import io.micrometer.core.instrument.MeterRegistry
import kotlinx.coroutines.CompletionHandler
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(OrderIndexerProperties::class)
class OrderIndexerConfiguration(
    environmentInfo: ApplicationEnvironmentInfo
) {

    private val logger = LoggerFactory.getLogger(OrderIndexerConfiguration::class.java)

    private val balanceConsumerGroup = "${environmentInfo.name}.protocol.solana.order.indexer.balance"

    @Bean
    fun orderBalanceEventConsumer(
        eventsConsumerFactory: SolanaEventsConsumerFactory,
        orderIndexerProperties: OrderIndexerProperties,
        orderBalanceConsumerEventHandler: OrderBalanceConsumerEventHandler,
        meterRegistry: MeterRegistry
    ): ConsumerWorkerHolder<BalanceEventDto> {
        val consumerBatchWorkers = (0 until orderIndexerProperties.orderBalanceConsumerWorkers).map { id ->
            val consumer = eventsConsumerFactory.createBalanceEventConsumer(
                consumerGroup = balanceConsumerGroup,
                clientIdSuffix = ".$id"
            )
            val workerName = "$balanceConsumerGroup.$id"
            ConsumerBatchWorker(
                consumer = consumer,
                eventHandler = orderBalanceConsumerEventHandler,
                workerName = workerName,
                properties = DaemonWorkerProperties(consumerBatchSize = orderIndexerProperties.orderBalanceConsumerBatchSize),
                meterRegistry = meterRegistry,
                completionHandler = object : CompletionHandler {
                    override fun invoke(cause: Throwable?) {
                        if (cause != null) {
                            logger.error("Order balance consumer worker $workerName has aborted with error", cause)
                        } else {
                            logger.info("Order balance consumer worker $workerName has finished")
                        }
                    }
                }
            )
        }
        return ConsumerWorkerHolder(consumerBatchWorkers)
    }

}