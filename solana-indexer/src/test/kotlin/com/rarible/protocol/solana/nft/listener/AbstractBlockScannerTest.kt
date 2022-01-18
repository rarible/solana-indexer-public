package com.rarible.protocol.solana.nft.listener

import com.rarible.core.test.ext.KafkaTest
import com.rarible.core.test.ext.MongoCleanup
import com.rarible.core.test.ext.MongoTest
import org.junit.jupiter.api.Assertions
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@MongoTest
@MongoCleanup
@ContextConfiguration(classes = [TestSolanaScannerConfiguration::class])
@SpringBootTest(
    classes = [NftListenerApplication::class],
    properties = [
        "application.environment = test",
        "spring.cloud.consul.config.enabled = false",
        "spring.cloud.service-registry.auto-registration.enabled = false",
        "spring.cloud.discovery.enabled = false",
        "logging.logstash.tcp-socket.enabled = false"
    ]
)
@ActiveProfiles("test")
@KafkaTest
@Testcontainers
abstract class AbstractBlockScannerTest {
    protected fun getWallet(config: String? = null): String {
        val args = buildList {
            add("spl-token")
            add("address")
            config?.let { add("/root/.config/solana/$it") }
        }

        return processOperation(args) { it.parse(0, -1) }
    }

    protected fun createWallet(name: String): String {
        val args = listOf("solana-keygen", "new", "--outfile", "/root/.config/solana/$name")

        return processOperation(args) { it.parse(4, -1) }
    }

    protected fun createToken(decimals: Int?): String {
        val args = buildList {
            add("spl-token")
            add("create-token")
            decimals?.let { add("--decimals"); add("$it") }
        }

        return processOperation(args) { it.parse(0, -1) }
    }

    protected fun createAccount(token: String, wallet: String? = null): String {
        val args = buildList {
            add("spl-token")
            add("create-account")
            add(token)
            wallet?.let { add("--owner"); add(it) }
        }

        return processOperation(args) { it.parse(0, -1) }
    }

    protected fun mintToken(token: String, amount: ULong): String {
        val args = listOf("spl-token", "mint", token, "$amount")

        return processOperation(args) { it.parse(0, -1) }
    }

    protected fun burnToken(tokenAccount: String, amount: ULong): String {
        val args = listOf("spl-token", "burn", tokenAccount, "$amount")

        return processOperation(args) { it.parse(0, -1) }
    }

    protected fun transferToken(token: String, amount: ULong, wallet: String): String {
        val args = listOf("spl-token", "transfer", token, "$amount", wallet)

        return processOperation(args) { it.parse(0, -1) }
    }

    private fun <T> processOperation(args: List<String>, parser: (String) -> T): T {
        val exec = solana.execInContainer(*args.toTypedArray())

        Assertions.assertEquals(0, exec.exitCode, exec.stderr)

        return parser(exec.stdout)
    }

    private fun String.parse(rowNumber: Int, colNumber: Int): String {
        val rows = split(System.lineSeparator())
        val i = (rowNumber + rows.size) % rows.size
        val cols = rows[i].split(" ")
        val j = (colNumber + cols.size) % cols.size

        return cols[j]
    }

    companion object {
        @Container
        val solana: GenericContainer<*> = GenericContainer(DockerImageName.parse("rarible/solana-docker-mac-m1"))
            .withExposedPorts(8899)
            .withCommand("solana-test-validator --limit-ledger-size=50_000_000")
            .waitingFor(Wait.defaultWaitStrategy())

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            val port = solana.getMappedPort(8899)

            registry.add("blockchain.scanner.solana.rpcApiUrl") { "http://127.0.0.1:$port" }
        }
    }
}