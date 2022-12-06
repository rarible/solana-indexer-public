package com.rarible.protocol.solana.nft.api.test

import com.rarible.core.test.ext.KafkaTest
import com.rarible.core.test.ext.MongoCleanup
import com.rarible.core.test.ext.MongoTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJson
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.junit.jupiter.Testcontainers

@KafkaTest
@MongoTest
@MongoCleanup
@Testcontainers
@AutoConfigureJson
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = [
        "application.environment=e2e",
        "spring.cloud.service-registry.auto-registration.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "spring.cloud.consul.config.enabled=false",
        "logging.logjson.enabled = false",
        "logging.logstash.tcp-socket.enabled=false",
        "logging.level.org.springframework.web=DEBUG",
        "logging.level.org.springframework.data.mongodb.core.ReactiveMongoTemplate=DEBUG"
    ]
)
@ActiveProfiles("integration")
abstract class AbstractIntegrationTest {
    init {
        System.setProperty("spring.data.mongodb.database", "protocol")
    }

    @Autowired
    protected lateinit var mongo: ReactiveMongoOperations

    @LocalServerPort
    protected var port: Int = 0
}
