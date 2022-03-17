package com.rarible.protocol.solana.nft.listener

import com.rarible.blockchain.scanner.solana.model.SolanaLogRecord
import com.rarible.core.test.wait.Wait
import com.rarible.protocol.solana.nft.listener.service.records.SolanaAuctionHouseRecord
import com.rarible.protocol.solana.nft.listener.service.subscribers.SubscriberGroup
import com.rarible.protocol.solana.test.ANY_SOLANA_LOG
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.isEqualTo
import java.time.Duration
import java.time.Instant
import java.util.*

class AuctionHouseTest : AbstractBlockScannerTest() {
    private val timeout = Duration.ofSeconds(5)
    private val wrappedSol = "So11111111111111111111111111111111111111112"

    @Autowired
    private lateinit var mongo: ReactiveMongoOperations

    @Test
    fun createAuctionHouseTest() = runBlocking {
        val house = createAuctionHouse()
        val wallet = getWalletAddress()

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
    fun sellTest() = runBlocking {
        val house = createAuctionHouse()
        val token = mintNft()
        airdrop(10, getFeePayerAccountForAuctionHouse())
        sell(house, 1, token, 1)

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
                        mint = "",
                        amount = 1L.toBigInteger(),
                        sellPrice =  1.scaleSupply(9),
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
        val house = createAuctionHouse()
        val token = mintNft()
        airdrop(10, getFeePayerAccountForAuctionHouse())
        buy(house, 1, token, 1)

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
                        mint = "",
                        amount = 1L.toBigInteger(),
                        buyPrice =  1.scaleSupply(9),
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
        val aliceWallet = createFileWallet("${UUID.randomUUID()}")
        val token = mintNft()
        val house = createAuctionHouse()

        airdrop(10, getFeePayerAccountForAuctionHouse())
        airdrop(10, aliceWallet)

        sell(house, 1, token, 1)
        buy(house, 1, token, 1, aliceWallet)

        executeSale(house, 1, token, 1, buyerWallet = getWalletAddress(aliceWallet), sellerWallet = getWalletAddress())

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
                        mint = token,
                        amount = 1L.toBigInteger(),
                        price =  1.scaleSupply(9),
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