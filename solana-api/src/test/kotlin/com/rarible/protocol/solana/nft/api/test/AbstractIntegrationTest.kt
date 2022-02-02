package com.rarible.protocol.solana.nft.api.test

import com.rarible.core.test.ext.KafkaTest
import com.rarible.core.test.ext.MongoCleanup
import com.rarible.core.test.ext.MongoTest
import com.rarible.solana.protocol.api.client.BalanceControllerApi
import com.rarible.solana.protocol.api.client.FixedSolanaApiServiceUriProvider
import com.rarible.solana.protocol.api.client.NoopWebClientCustomizer
import com.rarible.solana.protocol.api.client.SolanaNftIndexerApiClientFactory
import com.rarible.solana.protocol.api.client.TokenControllerApi
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJson
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.junit.jupiter.Testcontainers
import java.net.URI
import javax.annotation.PostConstruct

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
        "logging.logstash.tcp-socket.enabled=false",
        "logging.level.org.springframework.web=DEBUG",
        "logging.level.org.springframework.data.mongodb.core.ReactiveMongoTemplate=DEBUG"
    ]
)
@ActiveProfiles("integration")
@Import(TestPropertiesConfiguration::class)
abstract class AbstractIntegrationTest {
    init {
        System.setProperty("spring.data.mongodb.database", "protocol")
    }

    protected lateinit var tokenControllerApi: TokenControllerApi

    protected lateinit var balanceControllerApi: BalanceControllerApi

    @Autowired
    protected lateinit var mongo: ReactiveMongoOperations

    @LocalServerPort
    private var port: Int = 0

    @PostConstruct
    fun setup() {
        val urlProvider = FixedSolanaApiServiceUriProvider(URI.create("http://127.0.0.1:$port"))
        val clientFactory = SolanaNftIndexerApiClientFactory(urlProvider, NoopWebClientCustomizer())
        tokenControllerApi = clientFactory.createTokenControllerApiClient()
        balanceControllerApi = clientFactory.createBalanceControllerApiClient()
    }
}
