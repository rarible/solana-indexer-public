package com.rarible.protocol.solana.nft.listener

import com.rarible.blockchain.scanner.solana.model.SolanaLogRecord
import com.rarible.core.test.data.randomString
import com.rarible.core.test.wait.Wait
import com.rarible.protocol.solana.common.model.WrappedSolAssetType
import com.rarible.protocol.solana.common.records.SolanaAuctionHouseOrderRecord
import com.rarible.protocol.solana.common.records.SolanaAuctionHouseRecord
import com.rarible.protocol.solana.common.records.SolanaBalanceRecord
import com.rarible.protocol.solana.nft.listener.service.subscribers.SubscriberGroup
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
import java.time.Duration
import java.time.Instant

class AuctionHouseTest : AbstractBlockScannerTest() {

    private val timeout = Duration.ofSeconds(5)
    private val wrappedSol = WrappedSolAssetType.tokenAddress

    @Autowired
    private lateinit var mongo: ReactiveMongoOperations

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
                        auctionHouse = house,
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
        airdrop(10, getFeePayerAccountForAuctionHouse(house, keypair))
        sell(house, baseKeypair, 1, token, 1)

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
                        auctionHouse = house,
                        log = ANY_SOLANA_LOG,
                        timestamp = Instant.EPOCH
                    )
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
        airdrop(10, getFeePayerAccountForAuctionHouse(house, keypair))
        buy(house, keypair, 1, token, 1)

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
                        auctionHouse = house,
                        log = ANY_SOLANA_LOG,
                        timestamp = Instant.EPOCH
                    )
                )
            )
        }
    }

    @Test
    fun executeSaleTest() = runBlocking {
        val auctionHouseKeypair = createKeypair(randomString())
        airdrop(10, getWallet(auctionHouseKeypair))
        val house = createAuctionHouse(auctionHouseKeypair)
        val sellerWallet = getWallet(baseKeypair)

        val buyerKeypair = createKeypair(randomString())
        airdrop(10, buyerKeypair)
        val buyerWallet = getWallet(buyerKeypair)
        val token = mintNft(keypair = baseKeypair)

        airdrop(10, getFeePayerAccountForAuctionHouse(house, auctionHouseKeypair))
        airdrop(10, buyerKeypair)

        sell(house, baseKeypair, 1, token, 1)
        buy(house, buyerKeypair, 1, token, 1)

        executeSale(house, auctionHouseKeypair, 1, token, 1, buyerWallet = buyerWallet, sellerWallet = sellerWallet)
        Wait.waitAssert(timeout) {
            val balanceRecords  = findRecordByType(
                collection = SubscriberGroup.BALANCE.collectionName,
                type = SolanaBalanceRecord.InitializeBalanceAccountRecord::class.java
            ).toList()
            val fromAccount = balanceRecords.single { it.owner == sellerWallet }.balanceAccount
            val toAccount = balanceRecords.single { it.owner == buyerWallet }.balanceAccount
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
                        buyPrice = 1.scaleSupply(9),
                        tokenAccount = fromAccount,
                        mint = token,
                        amount = 1L.toBigInteger(),
                        auctionHouse = house,
                        log = ANY_SOLANA_LOG,
                        timestamp = Instant.EPOCH,
                    )
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
                        sellPrice = 1.scaleSupply(9),
                        tokenAccount = fromAccount,
                        mint = token,
                        amount = 1L.toBigInteger(),
                        auctionHouse = house,
                        log = ANY_SOLANA_LOG,
                        timestamp = Instant.EPOCH,
                    )
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
                price = 1.scaleSupply(9),
                auctionHouse = house,
                log = ANY_SOLANA_LOG,
                timestamp = Instant.EPOCH,
                direction = SolanaAuctionHouseOrderRecord.ExecuteSaleRecord.Direction.SELL,
                treasuryMint = wrappedSol
            )
            assertThat(saleRecords).usingElementComparatorIgnoringFields(
                SolanaAuctionHouseOrderRecord.ExecuteSaleRecord::log.name,
                SolanaAuctionHouseOrderRecord.ExecuteSaleRecord::timestamp.name
            ).isEqualTo(
                listOf(
                    sellRecord,
                    sellRecord.copy(direction = SolanaAuctionHouseOrderRecord.ExecuteSaleRecord.Direction.BUY)
                )
            )

            val incomeTransfersRecords  = findRecordByType(
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
                        owner = toAccount,
                        mint = token,
                        incomeAmount = 1.toBigInteger(),
                        log = ANY_SOLANA_LOG,
                        timestamp = Instant.EPOCH,
                    )
                )
            )

            val outcomeTransfersRecords  = findRecordByType(
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
                        owner = fromAccount,
                        mint = token,
                        outcomeAmount = 1.toBigInteger(),
                        log = ANY_SOLANA_LOG,
                        timestamp = Instant.EPOCH,
                    )
                )
            )
        }
    }

    private inline fun <reified T : SolanaLogRecord> findRecordByType(
        collection: String,
        type: Class<T>
    ): Flow<T> {
        val criteria = Criteria.where("_class").isEqualTo(T::class.java.name)

        return mongo.find(Query(criteria), type, collection).asFlow()
    }
}
