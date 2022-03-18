package com.rarible.protocol.solana.nft.listener

import com.rarible.blockchain.scanner.block.Block
import com.rarible.blockchain.scanner.block.BlockRepository
import com.rarible.blockchain.scanner.block.BlockStatus
import com.rarible.blockchain.scanner.solana.client.SolanaClient
import com.rarible.core.test.containers.KGenericContainer
import com.rarible.core.test.ext.KafkaTest
import com.rarible.core.test.ext.MongoCleanup
import com.rarible.core.test.ext.MongoTest
import com.rarible.core.test.ext.RedisTest
import com.rarible.protocol.solana.common.meta.MetaplexOffChainMetaLoader
import com.rarible.protocol.solana.common.meta.TokenMetaService
import com.rarible.protocol.solana.common.repository.BalanceRepository
import com.rarible.protocol.solana.common.repository.MetaplexMetaRepository
import com.rarible.protocol.solana.common.repository.MetaplexOffChainMetaRepository
import com.rarible.protocol.solana.common.repository.TokenRepository
import com.rarible.protocol.solana.nft.listener.service.records.SolanaBalanceRecord
import com.rarible.protocol.solana.nft.listener.service.subscribers.SolanaProgramId
import com.rarible.protocol.solana.nft.listener.service.subscribers.SubscriberGroup
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coJustRun
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.junit.jupiter.Testcontainers
import java.math.BigInteger

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
@RedisTest
@Testcontainers
abstract class AbstractBlockScannerTest {
    @Autowired
    private lateinit var mongo: ReactiveMongoOperations

    @Autowired
    private lateinit var client: SolanaClient

    @Autowired
    protected lateinit var metaplexMetaRepository: MetaplexMetaRepository

    @Autowired
    protected lateinit var tokenMetaService: TokenMetaService

    @Qualifier("test.solana.meta.loader")
    @Autowired
    protected lateinit var testMetaplexOffChainMetaLoader: MetaplexOffChainMetaLoader

    @Autowired
    @Qualifier("test.metaplex.off.chain.meta.repository")
    protected lateinit var metaplexOffChainMetaRepository: MetaplexOffChainMetaRepository

    @Autowired
    protected lateinit var tokenRepository: TokenRepository

    @Autowired
    protected lateinit var balanceRepository: BalanceRepository

    @Autowired
    private lateinit var repository: BlockRepository

    @BeforeEach
    fun cleanDatabase() = runBlocking<Unit> {
        val slot = client.getLatestSlot()
        val currentBlock = client.getBlock(slot) ?: error("Can't get latest block")

        runCatching {
            repository.save(
                Block(
                    id = currentBlock.number,
                    hash = currentBlock.hash,
                    parentHash = currentBlock.parentHash,
                    timestamp = currentBlock.timestamp,
                    status = BlockStatus.SUCCESS
                )
            )
        }
    }

    @BeforeEach
    fun cleanupMocks() {
        clearMocks(metaplexOffChainMetaRepository)
        coJustRun { metaplexOffChainMetaRepository.createIndexes() }
        coEvery { metaplexOffChainMetaRepository.findByTokenAddress(any()) } returns null
        coEvery { metaplexOffChainMetaRepository.findByOffChainCollectionHash(any()) } returns emptyFlow()
        coEvery { metaplexOffChainMetaRepository.save(any()) } throws (UnsupportedOperationException())
    }

    protected fun airdrop(sol: Int, address: String): String {
        val args = buildList {
            add("solana")
            add("airdrop")
            add("$sol")
            add(address)
        }

        return processOperation(args) { it.parse(0, -1) }
    }

    protected fun createAuctionHouse(): String {
        val args = buildList {
            add("ts-node")
            add("/home/solana/metaplex/js/packages/cli/src/auction-house-cli.ts")
            add("create_auction_house")
            add("-e")
            add("local")
            add("-sfbp")
            add("1000")
            add("-ccsp")
            add("false")
            add("-rso")
            add("-false")
            add("--keypair")
            add("/home/solana/.config/solana/id.json")
        }

        return processOperation(args) { it.parse(4, -1) }
    }

    protected fun getFeePayerAccountForAuctionHouse(): String {
        val args = buildList {
            add("ts-node")
            add("/home/solana/metaplex/js/packages/cli/src/auction-house-cli.ts")
            add("show")
            add("-e")
            add("local")
            add("--keypair")
            add("/home/solana/.config/solana/id.json")
        }

        return processOperation(args) { it.parse(8, -1) }
    }

    protected fun updateAuctionHouse(auctionHouse: String) {
        val args = buildList {
            add("ts-node")
            add("/home/solana/metaplex/js/packages/cli/src/auction-house-cli.ts")
            add("update_auction_house")
            add("-e")
            add("local")
            add("-ah")
            add(auctionHouse)
            add("--keypair")
            add("/home/solana/.config/solana/id.json")
        }

        return processOperation(args) { it.parse(4, -1) }
    }

    protected fun sell(
        auctionHouse: String,
        buyPrice: Long,
        mint: String,
        amount: Long,
        wallet: String = "/home/solana/.config/solana/id.json"
    ) {
        val args = buildList {
            add("ts-node")
            add("/home/solana/metaplex/js/packages/cli/src/auction-house-cli.ts")
            add("sell")
            add("-env")
            add("local")
            add("-ah")
            add(auctionHouse)
            add("--buy-price")
            add("$buyPrice")
            add("--mint")
            add(mint)
            add("--token-size")
            add("$amount")
            add("--keypair")
            add(wallet)
        }

        return processOperation(args) { it.parse(4, -1) }
    }

    protected fun buy(
        auctionHouse: String,
        buyPrice: Long,
        mint: String,
        amount: Long,
        wallet: String = "/home/solana/.config/solana/id.json"
    ) {
        val args = buildList {
            add("ts-node")
            add("/home/solana/metaplex/js/packages/cli/src/auction-house-cli.ts")
            add("buy")
            add("-e")
            add("local")
            add("-ah")
            add(auctionHouse)
            add("--buy-price")
            add("$buyPrice")
            add("--mint")
            add(mint)
            add("--token-size")
            add("$amount")
            add("--keypair")
            add(wallet)
        }

        return processOperation(args) { it.parse(4, -1) }
    }

    protected fun executeSale(
        auctionHouse: String,
        buyPrice: Long,
        mint: String,
        amount: Long,
        buyerWallet: String,
        sellerWallet: String,
    ) {
        val args = buildList {
            add("ts-node")
            add("/home/solana/metaplex/js/packages/cli/src/auction-house-cli.ts")
            add("execute_sale")
            add("-e")
            add("local")
            add("-ah")
            add(auctionHouse)
            add("--buy-price")
            add("$buyPrice")
            add("--mint")
            add(mint)
            add("--token-size")
            add("$amount")
            add("--buyer-wallet")
            add(buyerWallet)
            add("--seller-wallet")
            add(sellerWallet)
            add("--keypair")
            add("/home/solana/.config/solana/id.json")
        }

        return processOperation(args) { it.parse(4, -1) }
    }

    protected fun verifyCollection(mint: String, collection: String): String {
        val args = buildList {
            add("ts-node")
            add("/home/solana/metaplex/js/packages/cli/src/cli-nft.ts")
            add("verify-collection")
            add("-e")
            add("local")
            add("-m")
            add(mint)
            add("-c")
            add(collection)
            add("--keypair")
            add("/home/solana/.config/solana/id.json")
        }

        return processOperation(args) { it.parse(4, -1) }
    }

    protected fun mintNft(collection: String? = null): String {
        val args = buildList {
            add("ts-node")
            add("/home/solana/metaplex/js/packages/cli/src/cli-nft.ts")
            add("mint")
            add("-e")
            add("local")
            collection?.let {
                add("-c")
                add(it)
            }
            add("-u")
            add("https://gist.githubusercontent.com/enslinmike/a18bd9fa8e922d641a8a8a64ce84dea6/raw/a8298b26e47f30279a1b107f19287be4f198e21d/meta.json")
            add("--keypair")
            add("/home/solana/.config/solana/id.json")
        }

        return processOperation(args) { it.parse(4, -1) }
    }

    protected fun getWalletAddress(config: String? = null): String {
        val args = buildList {
            add("solana")
            add("address")
            config?.let {
                add("-k")
                add(it)
            }
        }

        return processOperation(args) { it.parse(0, -1) }
    }

    protected fun createWallet(name: String): String {
        val args = listOf("solana-keygen", "new", "--outfile", "/home/solana/.config/solana/$name")

        return processOperation(args) { it.parse(4, -1) }
    }

    protected fun createFileWallet(name: String): String {
        val args = listOf("solana-keygen", "new", "--outfile", "/home/solana/.config/solana/$name")

        return processOperation(args) { "/home/solana/.config/solana/$name" }
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

    protected suspend fun saveBalanceRecord(record: SolanaBalanceRecord): SolanaBalanceRecord {
        return mongo.save(record, SubscriberGroup.BALANCE.collectionName).awaitFirst()
    }

    private fun <T> processOperation(args: List<String>, parser: (String) -> T): T {
        val exec = solana.execInContainer(*args.toTypedArray())
        assertEquals(0, exec.exitCode, exec.stderr)
        return parser(exec.stdout)
    }

    private fun String.parse(rowNumber: Int, colNumber: Int): String {
        val rows = split(System.lineSeparator())
        val i = (rowNumber + rows.size) % rows.size
        val cols = rows[i].split(" ")
        val j = (colNumber + cols.size) % cols.size

        return cols[j]
    }

    protected fun Int.scaleSupply(decimals: Int): BigInteger =
        BigInteger.valueOf(this.toLong()) * BigInteger.TEN.pow(decimals)

    companion object {
        val solana: GenericContainer<*> = KGenericContainer("rarible/solana")
            .withExposedPorts(8899)
            .withCommand("solana-test-validator --no-bpf-jit --limit-ledger-size=50_000_000 --bpf-program ${SolanaProgramId.TOKEN_METADATA_PROGRAM} /home/solana/mpl_token_metadata.so --bpf-program ${SolanaProgramId.AUCTION_HOUSE_PROGRAM} /home/solana/mpl_auction_house.so")
            .waitingFor(Wait.defaultWaitStrategy())

        @BeforeAll
        @JvmStatic
        fun setUp() {
            solana.start()
            val exec = solana.execInContainer(
                "bash",
                "-c",
                "cd tmp && anchor idl init ${SolanaProgramId.AUCTION_HOUSE_PROGRAM} -f /home/solana/auction_house.json"
            )
            assertThat(exec.exitCode).isEqualTo(0).withFailMessage { exec.stderr }
            Thread.sleep(3000) // this is for tests
        }

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            val port = solana.getMappedPort(8899)

            registry.add("blockchain.scanner.solana.rpcApiUrls") { "http://127.0.0.1:$port" }
        }
    }
}
