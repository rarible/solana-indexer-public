package com.rarible.protocol.solana.nft.listener

import com.rarible.blockchain.scanner.solana.EnableSolanaScanner
import com.rarible.core.kafka.RaribleKafkaConsumer
import com.rarible.protocol.union.subscriber.SolanaEventsConsumerFactory
import com.rarible.solana.protocol.dto.BalanceEventDto
import com.rarible.solana.protocol.dto.TokenEventDto
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableSolanaScanner
@EnableAutoConfiguration
class TestSolanaScannerConfiguration {
    @Bean
    fun meterRegistry(): MeterRegistry = SimpleMeterRegistry()

    @Bean
    fun tokenEventConsumer(solanaEventsConsumerFactory: SolanaEventsConsumerFactory): RaribleKafkaConsumer<TokenEventDto> =
        solanaEventsConsumerFactory.createTokenEventConsumer("test")

    @Bean
    fun balanceEventConsumer(solanaEventsConsumerFactory: SolanaEventsConsumerFactory): RaribleKafkaConsumer<BalanceEventDto> =
        solanaEventsConsumerFactory.createBalanceEventConsumer("test")
}
