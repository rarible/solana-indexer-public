package com.rarible.protocol.solana.nft.listener

import com.rarible.blockchain.scanner.solana.model.SolanaLogRecord
import com.rarible.core.test.data.randomString
import com.rarible.core.test.wait.Wait
import com.rarible.protocol.solana.common.model.Asset
import com.rarible.protocol.solana.common.model.Order
import com.rarible.protocol.solana.common.model.OrderStatus
import com.rarible.protocol.solana.common.model.TokenNftAssetType
import com.rarible.protocol.solana.common.model.WrappedSolAssetType
import com.rarible.protocol.solana.common.records.OrderDirection
import com.rarible.protocol.solana.common.records.SolanaAuctionHouseOrderRecord
import com.rarible.protocol.solana.common.records.SolanaAuctionHouseRecord
import com.rarible.protocol.solana.common.records.SolanaBalanceRecord
import com.rarible.protocol.solana.common.records.SubscriberGroup
import com.rarible.protocol.solana.common.repository.OrderRepository
import com.rarible.protocol.solana.test.ANY_SOLANA_LOG
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.isEqualTo
import java.math.BigDecimal
import java.math.BigInteger
import java.time.Duration
import java.time.Instant

class AuctionHouseTest : AbstractBlockScannerTest() {

    private val timeout = Duration.ofSeconds(5)
    private val wrappedSol = WrappedSolAssetType.SOL

    @Autowired
    private lateinit var mongo: ReactiveMongoOperations

    @Autowired
    private lateinit var orderRepository: OrderRepository

    @Test
    fun createAuctionHouseTest() = runBlocking {
        val keypair = createKeypair(randomString())
        val wallet = getWallet(keypair)
        airdrop(10, wallet)
        val house = createAuctionHouse(keypair)

        Wait.waitAssert(timeout) {
            val records = findRecordByType(
                collection = SubscriberGroup.AUCTION_HOUSE.collectionName,
                type = SolanaAuctionHouseRecord.CreateAuctionHouseRecord::class.java
            ).toList()

            assertThat(records).usingElementComparatorIgnoringFields(
                SolanaAuctionHouseRecord.CreateAuctionHouseRecord::log.name,
                SolanaAuctionHouseRecord.CreateAuctionHouseRecord::timestamp.name
            ).isEqualTo(
                listOf(
                    SolanaAuctionHouseRecord.CreateAuctionHouseRecord(
                        treasuryMint = wrappedSol,
                        feeWithdrawalDestination = wallet,
                        treasuryWithdrawalDestination = wallet,
                        auctionHouse = house.id,
                        log = ANY_SOLANA_LOG,
                        timestamp = Instant.EPOCH
                    )
                )
            )
        }
    }

    // NOTE! this test may be very slow (3+ minutes), it is OK.
    @Test
    fun sellTest() = runBlocking {
        val keypair = createKeypair(randomString())
        val wallet = getWallet(keypair)
        airdrop(10, wallet)
        val house = createAuctionHouse(keypair)
        val token = mintNft(baseKeypair)
        airdrop(10, house.feePayerAcct)
        sell(house.id, baseKeypair, 1, token, 1)

        Wait.waitAssert(timeout) {
            val records = findRecordByType(
                collection = SubscriberGroup.AUCTION_HOUSE_ORDER.collectionName,
                type = SolanaAuctionHouseOrderRecord.SellRecord::class.java
            ).toList()

            assertThat(records).usingElementComparatorIgnoringFields(
                SolanaAuctionHouseOrderRecord.SellRecord::log.name,
                SolanaAuctionHouseOrderRecord.SellRecord::timestamp.name,
                SolanaAuctionHouseOrderRecord.SellRecord::tokenAccount.name // TODO: do not ignore. We can calculate this field by [token] and [baseKeyPair]
            ).isEqualTo(
                listOf(
                    SolanaAuctionHouseOrderRecord.SellRecord(
                        maker = getWallet(baseKeypair),
                        tokenAccount = "",
                        mint = token,
                        amount = 1L.toBigInteger(),
                        sellPrice = 1.scaleSupply(9),
                        auctionHouse = house.id,
                        log = ANY_SOLANA_LOG,
                        timestamp = Instant.EPOCH,
                        orderId = ""
                    ).withUpdatedOrderId()
                )
            )
        }
    }

    @Test
    fun sellCancelTest() = runBlocking {
        val keypair = createKeypair(randomString())
        val wallet = getWallet(keypair)
        airdrop(10, wallet)
        val house = createAuctionHouse(keypair)
        val token = mintNft(baseKeypair)
        val sellerWallet = getWallet(baseKeypair)

        airdrop(10, house.feePayerAcct)
        sell(house.id, baseKeypair, 5, token, 1)
        Wait.waitAssert(timeout) {
            val order = orderRepository.findById(
                Order.calculateAuctionHouseOrderId(
                    maker = sellerWallet,
                    mint = token,
                    direction = OrderDirection.SELL,
                    auctionHouse = house.id
                )
            )
            assertThat(order)
                .usingRecursiveComparison()
                .ignoringFields(
                    "createdAt",
                    "updatedAt",
                    "revertableEvents"
                )
                .isEqualTo(
                    Order(
                        auctionHouse = house.id,
                        maker = sellerWallet,
                        status = OrderStatus.ACTIVE,
                        make = Asset(TokenNftAssetType(token), 1.toBigInteger()),
                        take = Asset(WrappedSolAssetType(), 5.scaleSupply(9)),
                        fill = BigInteger.ZERO,
                        createdAt = Instant.EPOCH,
                        updatedAt = Instant.EPOCH,
                        revertableEvents = emptyList(),
                        direction = OrderDirection.SELL,
                        makePrice = 5.scaleSupply(9).toBigDecimal(9),
                        takePrice = null
                    )
                )
        }
        cancel(house.id, baseKeypair, 5, token, 1)

        Wait.waitAssert(timeout) {
            val records = findRecordByType(
                collection = SubscriberGroup.AUCTION_HOUSE_ORDER.collectionName,
                type = SolanaAuctionHouseOrderRecord.CancelRecord::class.java
            ).toList()
            val cancelRecord = SolanaAuctionHouseOrderRecord.CancelRecord(
                mint = token,
                amount = 1L.toBigInteger(),
                auctionHouse = house.id,
                log = ANY_SOLANA_LOG,
                timestamp = Instant.EPOCH,
                orderId = "",
                maker = sellerWallet,
                price = 5.scaleSupply(9),
                direction = OrderDirection.SELL
            ).withUpdatedOrderId()

            assertThat(records).usingElementComparatorIgnoringFields(
                SolanaAuctionHouseOrderRecord.BuyRecord::log.name,
                SolanaAuctionHouseOrderRecord.BuyRecord::timestamp.name,
            ).containsExactlyInAnyOrder(
                cancelRecord,
                cancelRecord.copy(direction = OrderDirection.BUY).withUpdatedOrderId()
            )

            val order = orderRepository.findById(
                Order.calculateAuctionHouseOrderId(
                    maker = sellerWallet,
                    mint = token,
                    direction = OrderDirection.SELL,
                    auctionHouse = house.id
                )
            )
            assertThat(order)
                .usingRecursiveComparison()
                .ignoringFields(
                    "createdAt",
                    "updatedAt",
                    "revertableEvents"
                )
                .isEqualTo(
                    Order(
                        auctionHouse = house.id,
                        maker = sellerWallet,
                        status = OrderStatus.CANCELLED,
                        make = Asset(TokenNftAssetType(token), 1.toBigInteger()),
                        take = Asset(WrappedSolAssetType(), 5.scaleSupply(9)),
                        fill = BigInteger.ZERO,
                        createdAt = Instant.EPOCH,
                        updatedAt = Instant.EPOCH,
                        revertableEvents = emptyList(),
                        direction = OrderDirection.SELL,
                        makePrice = 5.scaleSupply(9).toBigDecimal(9),
                        takePrice = null
                    )
                )
        }
    }

    @Test
    fun buyCancelTest() = runBlocking {
        val keypair = createKeypair(randomString())
        val wallet = getWallet(keypair)
        airdrop(10, wallet)
        val house = createAuctionHouse(keypair)
        val token = mintNft(baseKeypair)
        airdrop(10, house.feePayerAcct)
        buy(house.id, keypair, 5, token, 1)

        Wait.waitAssert(timeout) {
            val order = orderRepository.findById(
                Order.calculateAuctionHouseOrderId(
                    maker = wallet,
                    mint = token,
                    direction = OrderDirection.BUY,
                    auctionHouse = house.id
                )
            )
            assertThat(order)
                .usingRecursiveComparison()
                .ignoringFields(
                    "createdAt",
                    "updatedAt",
                    "revertableEvents"
                )
                .isEqualTo(
                    Order(
                        auctionHouse = house.id,
                        maker = wallet,
                        status = OrderStatus.ACTIVE,
                        make = Asset(WrappedSolAssetType(), 5.scaleSupply(9)),
                        take = Asset(TokenNftAssetType(token), 1.toBigInteger()),
                        fill = BigInteger.ZERO,
                        createdAt = Instant.EPOCH,
                        updatedAt = Instant.EPOCH,
                        revertableEvents = emptyList(),
                        direction = OrderDirection.BUY,
                        makePrice = null,
                        takePrice = 5.scaleSupply(9).toBigDecimal(9)
                    )
                )
        }
        cancel(house.id, keypair, 5, token, 1)

        Wait.waitAssert(timeout) {
            val records = findRecordByType(
                collection = SubscriberGroup.AUCTION_HOUSE_ORDER.collectionName,
                type = SolanaAuctionHouseOrderRecord.CancelRecord::class.java
            ).toList()
            val cancelRecord = SolanaAuctionHouseOrderRecord.CancelRecord(
                mint = token,
                amount = 1L.toBigInteger(),
                auctionHouse = house.id,
                log = ANY_SOLANA_LOG,
                timestamp = Instant.EPOCH,
                orderId = "",
                maker = wallet,
                price = 5.scaleSupply(9),
                direction = OrderDirection.SELL
            ).withUpdatedOrderId()

            assertThat(records).usingElementComparatorIgnoringFields(
                SolanaAuctionHouseOrderRecord.BuyRecord::log.name,
                SolanaAuctionHouseOrderRecord.BuyRecord::timestamp.name,
            ).containsExactlyInAnyOrder(
                cancelRecord,
                cancelRecord.copy(direction = OrderDirection.BUY).withUpdatedOrderId()
            )

            val order = orderRepository.findById(
                Order.calculateAuctionHouseOrderId(
                    maker = wallet,
                    mint = token,
                    direction = OrderDirection.BUY,
                    auctionHouse = house.id
                )
            )
            assertThat(order)
                .usingRecursiveComparison()
                .ignoringFields(
                    "createdAt",
                    "updatedAt",
                    "revertableEvents"
                )
                .isEqualTo(
                    Order(
                        auctionHouse = house.id,
                        maker = wallet,
                        status = OrderStatus.CANCELLED,
                        make = Asset(WrappedSolAssetType(), 5.scaleSupply(9)),
                        take = Asset(TokenNftAssetType(token), 1.toBigInteger()),
                        fill = BigInteger.ZERO,
                        createdAt = Instant.EPOCH,
                        updatedAt = Instant.EPOCH,
                        revertableEvents = emptyList(),
                        direction = OrderDirection.BUY,
                        makePrice = null,
                        takePrice = 5.scaleSupply(9).toBigDecimal(9)
                    )
                )
        }
    }

    @Test
    fun buyTest() = runBlocking {
        val keypair = createKeypair(randomString())
        val wallet = getWallet(keypair)
        airdrop(10, wallet)
        val house = createAuctionHouse(keypair)
        val token = mintNft(baseKeypair)
        airdrop(10, house.feePayerAcct)
        buy(house.id, keypair, 1, token, 1)

        Wait.waitAssert(timeout) {
            val records = findRecordByType(
                collection = SubscriberGroup.AUCTION_HOUSE_ORDER.collectionName,
                type = SolanaAuctionHouseOrderRecord.BuyRecord::class.java
            ).toList()

            assertThat(records).usingElementComparatorIgnoringFields(
                SolanaAuctionHouseOrderRecord.BuyRecord::log.name,
                SolanaAuctionHouseOrderRecord.BuyRecord::timestamp.name,
                SolanaAuctionHouseOrderRecord.BuyRecord::tokenAccount.name // TODO: do not ignore. We can calculate this field by [token] and [baseKeyPair]
            ).isEqualTo(
                listOf(
                    SolanaAuctionHouseOrderRecord.BuyRecord(
                        maker = getWallet(keypair),
                        treasuryMint = wrappedSol,
                        tokenAccount = "",
                        mint = token,
                        amount = 1L.toBigInteger(),
                        buyPrice = 1.scaleSupply(9),
                        auctionHouse = house.id,
                        log = ANY_SOLANA_LOG,
                        timestamp = Instant.EPOCH,
                        orderId = ""
                    ).withUpdatedOrderId()
                )
            )
        }
    }

    @Test
    fun executeSaleTest() = runBlocking<Unit> {
        val auctionHouseKeypair = createKeypair(randomString())
        airdrop(10, getWallet(auctionHouseKeypair))
        val house = createAuctionHouse(auctionHouseKeypair)
        val sellerWallet = getWallet(baseKeypair)

        val buyerKeypair = createKeypair(randomString())
        airdrop(10, buyerKeypair)
        val buyerWallet = getWallet(buyerKeypair)
        val token = mintNft(keypair = baseKeypair)

        airdrop(10, house.feePayerAcct)
        airdrop(10, buyerKeypair)

        sell(house.id, baseKeypair, 5, token, 1)
        Wait.waitAssert(timeout) {
            val order = orderRepository.findById(
                Order.calculateAuctionHouseOrderId(
                    maker = sellerWallet,
                    mint = token,
                    direction = OrderDirection.SELL,
                    auctionHouse = house.id
                )
            )
            assertThat(order)
                .usingRecursiveComparison()
                .ignoringFields(
                    "createdAt",
                    "updatedAt",
                    "revertableEvents"
                )
                .isEqualTo(
                    Order(
                        auctionHouse = house.id,
                        maker = sellerWallet,
                        status = OrderStatus.ACTIVE,
                        make = Asset(TokenNftAssetType(token), 1.toBigInteger()),
                        take = Asset(WrappedSolAssetType(), 5.scaleSupply(9)),
                        fill = BigInteger.ZERO,
                        createdAt = Instant.EPOCH,
                        updatedAt = Instant.EPOCH,
                        revertableEvents = emptyList(),
                        direction = OrderDirection.SELL,
                        makePrice = 5.scaleSupply(9).toBigDecimal(9),
                        takePrice = null
                    )
                )
        }
        buy(house.id, buyerKeypair, 5, token, 1)
        val escrow = showEscrow(house.id, buyerKeypair, buyerWallet)
        assertThat(escrow).isEqualByComparingTo(5.scaleSupply(9).toBigDecimal())
        Wait.waitAssert(timeout) {
            val order = orderRepository.findById(
                Order.calculateAuctionHouseOrderId(
                    maker = buyerWallet,
                    mint = token,
                    direction = OrderDirection.BUY,
                    auctionHouse = house.id
                )
            )
            assertThat(order)
                .usingRecursiveComparison()
                .ignoringFields(
                    "createdAt",
                    "updatedAt",
                    "revertableEvents"
                )
                .isEqualTo(
                    Order(
                        auctionHouse = house.id,
                        maker = buyerWallet,
                        status = OrderStatus.ACTIVE,
                        make = Asset(WrappedSolAssetType(), 5.scaleSupply(9)),
                        take = Asset(TokenNftAssetType(token), 1.toBigInteger()),
                        fill = BigInteger.ZERO,
                        createdAt = Instant.EPOCH,
                        updatedAt = Instant.EPOCH,
                        revertableEvents = emptyList(),
                        direction = OrderDirection.BUY,
                        makePrice = null,
                        takePrice = 5.scaleSupply(9).toBigDecimal(9)
                    )
                )
        }
        val sellerBalanceBefore = getBalance(sellerWallet)
        executeSale(house.id, auctionHouseKeypair, 5, token, 1, buyerWallet = buyerWallet, sellerWallet = sellerWallet)
        Wait.waitAssert(timeout) {
            val balanceRecords = findRecordByType(
                collection = SubscriberGroup.BALANCE.collectionName,
                type = SolanaBalanceRecord.InitializeBalanceAccountRecord::class.java
            ).toList()
            val fromAccount = balanceRecords.single { it.owner == sellerWallet }.account
            val toAccount = balanceRecords.single { it.owner == buyerWallet }.account
            val buyRecords = findRecordByType(
                collection = SubscriberGroup.AUCTION_HOUSE_ORDER.collectionName,
                type = SolanaAuctionHouseOrderRecord.BuyRecord::class.java
            ).toList()
            assertThat(buyRecords).usingElementComparatorIgnoringFields(
                SolanaAuctionHouseOrderRecord.BuyRecord::log.name,
                SolanaAuctionHouseOrderRecord.BuyRecord::timestamp.name
            ).isEqualTo(
                listOf(
                    SolanaAuctionHouseOrderRecord.BuyRecord(
                        maker = buyerWallet,
                        treasuryMint = wrappedSol,
                        buyPrice = 5.scaleSupply(9),
                        tokenAccount = fromAccount,
                        mint = token,
                        amount = 1L.toBigInteger(),
                        auctionHouse = house.id,
                        log = ANY_SOLANA_LOG,
                        timestamp = Instant.EPOCH,
                        orderId = ""
                    ).withUpdatedOrderId()
                )
            )

            val sellRecords = findRecordByType(
                collection = SubscriberGroup.AUCTION_HOUSE_ORDER.collectionName,
                type = SolanaAuctionHouseOrderRecord.SellRecord::class.java
            ).toList()
            assertThat(sellRecords).usingElementComparatorIgnoringFields(
                SolanaAuctionHouseOrderRecord.SellRecord::log.name,
                SolanaAuctionHouseOrderRecord.SellRecord::timestamp.name
            ).isEqualTo(
                listOf(
                    SolanaAuctionHouseOrderRecord.SellRecord(
                        maker = sellerWallet,
                        sellPrice = 5.scaleSupply(9),
                        tokenAccount = fromAccount,
                        mint = token,
                        amount = 1L.toBigInteger(),
                        auctionHouse = house.id,
                        log = ANY_SOLANA_LOG,
                        timestamp = Instant.EPOCH,
                        orderId = ""
                    ).withUpdatedOrderId()
                )
            )

            val saleRecords = findRecordByType(
                collection = SubscriberGroup.AUCTION_HOUSE_ORDER.collectionName,
                type = SolanaAuctionHouseOrderRecord.ExecuteSaleRecord::class.java
            ).toList()
            val sellRecord = SolanaAuctionHouseOrderRecord.ExecuteSaleRecord(
                buyer = buyerWallet,
                seller = sellerWallet,
                mint = token,
                amount = 1L.toBigInteger(),
                price = 5.scaleSupply(9),
                auctionHouse = house.id,
                log = ANY_SOLANA_LOG,
                timestamp = Instant.EPOCH,
                direction = OrderDirection.SELL,
                treasuryMint = wrappedSol
            )
            assertThat(saleRecords).usingElementComparatorIgnoringFields(
                SolanaAuctionHouseOrderRecord.ExecuteSaleRecord::log.name,
                SolanaAuctionHouseOrderRecord.ExecuteSaleRecord::timestamp.name
            ).isEqualTo(
                listOf(
                    sellRecord,
                    sellRecord.copy(direction = OrderDirection.BUY)
                )
            )

            val incomeTransfersRecords = findRecordByType(
                collection = SubscriberGroup.BALANCE.collectionName,
                type = SolanaBalanceRecord.TransferIncomeRecord::class.java
            ).toList()
            assertThat(incomeTransfersRecords).usingElementComparatorIgnoringFields(
                SolanaBalanceRecord.TransferIncomeRecord::log.name,
                SolanaBalanceRecord.TransferIncomeRecord::timestamp.name
            ).isEqualTo(
                listOf(
                    SolanaBalanceRecord.TransferIncomeRecord(
                        from = fromAccount,
                        account = toAccount,
                        mint = token,
                        incomeAmount = 1.toBigInteger(),
                        log = ANY_SOLANA_LOG,
                        timestamp = Instant.EPOCH,
                    )
                )
            )

            val outcomeTransfersRecords = findRecordByType(
                collection = SubscriberGroup.BALANCE.collectionName,
                type = SolanaBalanceRecord.TransferOutcomeRecord::class.java
            ).toList()
            assertThat(outcomeTransfersRecords).usingElementComparatorIgnoringFields(
                SolanaBalanceRecord.TransferOutcomeRecord::log.name,
                SolanaBalanceRecord.TransferOutcomeRecord::timestamp.name
            ).isEqualTo(
                listOf(
                    SolanaBalanceRecord.TransferOutcomeRecord(
                        to = toAccount,
                        account = fromAccount,
                        mint = token,
                        outcomeAmount = 1.toBigInteger(),
                        log = ANY_SOLANA_LOG,
                        timestamp = Instant.EPOCH,
                    )
                )
            )

            val buyOrder = orderRepository.findById(
                Order.calculateAuctionHouseOrderId(
                    maker = buyerWallet,
                    mint = token,
                    direction = OrderDirection.BUY,
                    auctionHouse = house.id
                )
            )
            assertThat(buyOrder)
                .usingRecursiveComparison()
                .ignoringFields(
                    "createdAt",
                    "updatedAt",
                    "revertableEvents"
                )
                .isEqualTo(
                    Order(
                        auctionHouse = house.id,
                        maker = buyerWallet,
                        status = OrderStatus.FILLED,
                        make = Asset(WrappedSolAssetType(), 5.scaleSupply(9)),
                        take = Asset(TokenNftAssetType(token), 1.toBigInteger()),
                        fill = BigInteger.ONE,
                        createdAt = Instant.EPOCH,
                        updatedAt = Instant.EPOCH,
                        revertableEvents = emptyList(),
                        direction = OrderDirection.BUY,
                        makePrice = null,
                        takePrice = 5.scaleSupply(9).toBigDecimal(9)
                    )
                )

            val sellOrder = orderRepository.findById(
                Order.calculateAuctionHouseOrderId(
                    maker = sellerWallet,
                    mint = token,
                    direction = OrderDirection.SELL,
                    auctionHouse = house.id
                )
            )
            assertThat(sellOrder)
                .usingRecursiveComparison()
                .ignoringFields(
                    "createdAt",
                    "updatedAt",
                    "revertableEvents"
                )
                .isEqualTo(
                    Order(
                        auctionHouse = house.id,
                        maker = sellerWallet,
                        status = OrderStatus.FILLED,
                        make = Asset(TokenNftAssetType(token), 1.toBigInteger()),
                        take = Asset(WrappedSolAssetType(), 5.scaleSupply(9)),
                        fill = BigInteger.ONE,
                        createdAt = Instant.EPOCH,
                        updatedAt = Instant.EPOCH,
                        revertableEvents = emptyList(),
                        direction = OrderDirection.SELL,
                        makePrice = 5.scaleSupply(9).toBigDecimal(9),
                        takePrice = null
                    )
                )
        }

        assertThat(getBalance(sellerWallet) - sellerBalanceBefore).isEqualByComparingTo(BigDecimal.valueOf(4.5))
        assertThat(getBalance(house.treasuryAcct)).isEqualByComparingTo(BigDecimal.valueOf(0.5))
    }

    private inline fun <reified T : SolanaLogRecord> findRecordByType(
        collection: String,
        type: Class<T>
    ): Flow<T> {
        val criteria = Criteria.where("_class").isEqualTo(T::class.java.name)

        return mongo.find(Query(criteria), type, collection).asFlow()
    }
}
