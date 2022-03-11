package com.rarible.protocol.solana.nft.listener

import com.rarible.protocol.solana.common.configuration.CommonConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import

@SpringBootApplication
@Import(CommonConfiguration::class)
class NftListenerApplication

fun main(args: Array<String>) {
    if (System.getProperty("LOCAL") == "true") {
        setPropertiesToRunLocally()
    }
    runApplication<NftListenerApplication>(*args)
}

// Note! Also, to run locally you should start MongoDB & Kafka containers locally
// and tune parameters in the 'application-local.yml' (PLAINTEXT://localhost:53502, mongodb://localhost:53511)
private fun setPropertiesToRunLocally() {
    System.setProperty("spring.profiles.active", "local")
    System.setProperty("spring.cloud.service-registry.auto-registration.enabled", "false")
    System.setProperty("spring.cloud.discovery.enabled", "false")
    System.setProperty("spring.cloud.consul.config.enabled", "false")
    System.setProperty("logging.logstash.tcp-socket.enabled", "false")
    System.setProperty("application.environment", "local")
}
