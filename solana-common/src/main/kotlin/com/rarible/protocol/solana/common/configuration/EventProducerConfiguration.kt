package com.rarible.protocol.solana.common.configuration

import com.rarible.core.application.ApplicationEnvironmentInfo
import com.rarible.core.kafka.RaribleKafkaProducer
import com.rarible.core.kafka.json.JsonSerializer
import com.rarible.protocol.solana.common.update.PackageUpdate
import com.rarible.protocol.solana.dto.BalanceEventDto
import com.rarible.protocol.solana.dto.SolanaEventTopicProvider
import com.rarible.protocol.solana.dto.TokenEventDto
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(value = [SolanaIndexerProperties::class])
@ComponentScan(basePackageClasses = [PackageUpdate::class])
class EventProducerConfiguration(
    applicationEnvironmentInfo: ApplicationEnvironmentInfo,
    properties: SolanaIndexerProperties
) {

    private val env = applicationEnvironmentInfo.name
    private val producerBrokerReplicaSet = properties.kafkaReplicaSet

    @Bean
    fun tokenEventProducer(): RaribleKafkaProducer<TokenEventDto> {
        val tokenTopic = SolanaEventTopicProvider.getTokenTopic(env)
        return createSolanaProducer(
            clientSuffix = "token",
            topic = tokenTopic,
            type = TokenEventDto::class.java
        )
    }

    @Bean
    fun balanceEventProducer(): RaribleKafkaProducer<BalanceEventDto> {
        val balanceTopic = SolanaEventTopicProvider.getBalanceTopic(env)
        return createSolanaProducer(
            clientSuffix = "balance",
            topic = balanceTopic,
            type = BalanceEventDto::class.java
        )
    }

    private fun <T> createSolanaProducer(
        @Suppress("SameParameterValue") clientSuffix: String,
        topic: String,
        type: Class<T>
    ): RaribleKafkaProducer<T> = RaribleKafkaProducer(
        clientId = "${env}.protocol.solana.${clientSuffix}",
        valueSerializerClass = JsonSerializer::class.java,
        valueClass = type,
        defaultTopic = topic,
        bootstrapServers = producerBrokerReplicaSet
    )

}
