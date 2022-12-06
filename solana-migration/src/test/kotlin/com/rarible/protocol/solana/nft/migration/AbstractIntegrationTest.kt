package com.rarible.protocol.solana.nft.migration

import com.rarible.core.test.ext.KafkaTest
import com.rarible.core.test.ext.MongoCleanup
import com.rarible.core.test.ext.MongoTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.testcontainers.junit.jupiter.Testcontainers

@MongoTest
@KafkaTest
@MongoCleanup
@ContextConfiguration(classes = [TestConfiguration::class])
@SpringBootTest(
    classes = [NftMigrationApplication::class],
    properties = [
        "application.environment = test",
        "common.blockchain = solana",
        "spring.cloud.service-registry.auto-registration.enabled = false",
        "spring.cloud.discovery.enabled = false",
        "spring.cloud.consul.config.enabled = false",
        "logging.logjson.enabled = false",
        "logging.logstash.tcp-socket.enabled = false"
    ]
)
@ActiveProfiles("test")
@Testcontainers
abstract class AbstractIntegrationTest {
    @Autowired
    private lateinit var mongo: ReactiveMongoOperations
}