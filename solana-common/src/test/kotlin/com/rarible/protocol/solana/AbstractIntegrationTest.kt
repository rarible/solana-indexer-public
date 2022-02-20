package com.rarible.protocol.solana

import com.rarible.core.mongo.configuration.IncludePersistProperties
import com.rarible.core.test.ext.KafkaTest
import com.rarible.core.test.ext.MongoCleanup
import com.rarible.core.test.ext.MongoTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration

@MongoTest
@MongoCleanup
@KafkaTest
@SpringBootTest(
    properties = [
        "application.environment = e2e",
        "spring.application.name = test",
        "spring.cloud.consul.config.enabled = false",
        "spring.cloud.service-registry.auto-registration.enabled = false",
        "spring.cloud.discovery.enabled = false",
        "logging.logstash.tcp-socket.enabled = false"
    ]
)
@ActiveProfiles("test")
@ContextConfiguration(classes = [TestContext::class])
abstract class AbstractIntegrationTest {
    @Autowired
    lateinit var mongo: ReactiveMongoOperations
}

@Configuration
@EnableAutoConfiguration
@IncludePersistProperties
class TestContext
