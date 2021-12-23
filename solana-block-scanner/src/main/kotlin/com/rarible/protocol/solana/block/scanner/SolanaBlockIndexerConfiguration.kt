package com.rarible.protocol.solana.block.scanner

import com.github.cloudyrock.spring.v5.EnableMongock
import com.rarible.blockchain.scanner.reconciliation.DefaultReconciliationFormProvider
import com.rarible.blockchain.scanner.reconciliation.ReconciliationFromProvider
import com.rarible.blockchain.scanner.solana.EnableSolanaScanner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@EnableMongock
@Configuration
@EnableSolanaScanner
class SolanaBlockIndexerConfiguration {
    @Bean
    fun reconciliationFromProvider(): ReconciliationFromProvider {
        return DefaultReconciliationFormProvider()
    }
}