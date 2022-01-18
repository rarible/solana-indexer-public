package com.rarible.protocol.solana.nft.listener

import com.rarible.blockchain.scanner.solana.EnableSolanaScanner
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
}
