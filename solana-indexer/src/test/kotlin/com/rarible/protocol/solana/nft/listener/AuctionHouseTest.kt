package com.rarible.protocol.solana.nft.listener

import com.rarible.blockchain.scanner.solana.model.SolanaLogRecord
import com.rarible.core.test.data.randomString
import com.rarible.core.test.wait.Wait
import com.rarible.protocol.solana.nft.listener.service.records.SolanaAuctionHouseRecord
import com.rarible.protocol.solana.nft.listener.service.subscribers.SubscriberGroup
import com.rarible.protocol.solana.test.ANY_SOLANA_LOG
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Disabled
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
    private val wrappedSol = "So11111111111111111111111111111111111111112"

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

            Assertions.assertThat(records).usingElementComparatorIgnoringFields(
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

    @Test
    @Disabled //TODO endless test
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
                collection = SubscriberGroup.AUCTION_HOUSE.collectionName,
                type = SolanaAuctionHouseRecord.SellRecord::class.java
            ).toList()

            Assertions.assertThat(records).usingElementComparatorIgnoringFields(
                SolanaAuctionHouseRecord.SellRecord::log.name,
                SolanaAuctionHouseRecord.SellRecord::timestamp.name,
                SolanaAuctionHouseRecord.SellRecord::mint.name
            ).isEqualTo(
                listOf(
                    SolanaAuctionHouseRecord.SellRecord(
                        maker = getWallet(baseKeypair),
                        mint = "",
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
                collection = SubscriberGroup.AUCTION_HOUSE.collectionName,
                type = SolanaAuctionHouseRecord.BuyRecord::class.java
            ).toList()

            Assertions.assertThat(records).usingElementComparatorIgnoringFields(
                SolanaAuctionHouseRecord.BuyRecord::log.name,
                SolanaAuctionHouseRecord.BuyRecord::timestamp.name,
                SolanaAuctionHouseRecord.BuyRecord::mint.name
            ).isEqualTo(
                listOf(
                    SolanaAuctionHouseRecord.BuyRecord(
                        maker = getWallet(keypair),
                        mint = "",
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
    @Disabled //TODO endless test
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
            val records = findRecordByType(
                collection = SubscriberGroup.AUCTION_HOUSE.collectionName,
                type = SolanaAuctionHouseRecord.ExecuteSellRecord::class.java
            ).toList()

            Assertions.assertThat(records).usingElementComparatorIgnoringFields(
                SolanaAuctionHouseRecord.ExecuteSellRecord::log.name,
                SolanaAuctionHouseRecord.ExecuteSellRecord::timestamp.name
            ).isEqualTo(
                listOf(
                    SolanaAuctionHouseRecord.ExecuteSellRecord(
                        buyer = buyerWallet,
                        seller = sellerWallet,
                        mint = token,
                        amount = 1L.toBigInteger(),
                        price = 1.scaleSupply(9),
                        auctionHouse = house,
                        log = ANY_SOLANA_LOG,
                        timestamp = Instant.EPOCH
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