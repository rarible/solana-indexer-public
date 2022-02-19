package com.rarible.protocol.solana.nft.listener

import com.rarible.blockchain.scanner.solana.model.SolanaLogRecord
import com.rarible.core.test.wait.Wait
import com.rarible.protocol.solana.borsh.MetaplexMetadataProgram
import com.rarible.protocol.solana.nft.listener.service.subscribers.SubscriberGroup
import com.rarible.protocol.solana.nft.listener.service.records.SolanaBalanceRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaMetaRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaTokenRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.isEqualTo
import java.time.Duration
import java.util.*
import kotlin.math.pow

// TODO[tests]: compare the whole Record objects.
class SplProgramTest : AbstractBlockScannerTest() {
    private val timeout = Duration.ofSeconds(5)

    @Autowired
    private lateinit var mongo: ReactiveMongoOperations

    @Test
    fun createMetadata() = runBlocking {
        val wallet = getWallet()
        val nft = mintNft()

        Wait.waitAssert(timeout) {
            val records = findRecordByType(
                collection = SubscriberGroup.METAPLEX_META.collectionName,
                type = SolanaMetaRecord.MetaplexCreateMetadataRecord::class.java
            ).toList()

            assertEquals(1, records.size)
            val record = records.single()

            assertEquals(nft, record.mint)
            assertEquals(record.data.mutable, false)
            assertEquals(
                record.data.metadata, MetaplexMetadataProgram.Data(
                    name = "My NFT #1",
                    uri = "https://gist.githubusercontent.com/enslinmike/a18bd9fa8e922d641a8a8a64ce84dea6/raw/a8298b26e47f30279a1b107f19287be4f198e21d/meta.json",
                    symbol = "MY_SYMBOL",
                    sellerFeeBasisPoints = 420,
                    creators = listOf(MetaplexMetadataProgram.Creator(address = wallet, share = 100, verified = true)),
                    collection = null
                )
            )
        }
    }

    @Test
    fun verifyMetadata() = runBlocking {
        val collection = mintNft()
        val nft = mintNft(collection)

        Wait.waitAssert(timeout) {
            val meta = metaRepository.findByTokenAddress(nft) ?: fail("Meta not ready")

            assertFalse(meta.verified)
        }

        verifyCollection(nft, collection)

        Wait.waitAssert(timeout) {
            val meta = metaRepository.findByTokenAddress(nft) ?: fail("Meta not ready")

            assertTrue(meta.verified)
        }
    }

    @Test
    fun initializeMint() = runBlocking {
        val wallet = getWallet()
        val token = createToken(decimals = 3)

        Wait.waitAssert(timeout) {
            val records = findRecordByType(
                SubscriberGroup.TOKEN.collectionName,
                SolanaTokenRecord.InitializeMintRecord::class.java
            ).toList()

            assertEquals(1, records.size)
            val record = records.single()

            assertEquals(token, record.mint)
            assertEquals(wallet, record.mintAuthority)
            assertEquals(3, record.decimals)
        }
    }

    @Test
    fun createAccount() = runBlocking {
        val wallet = getWallet()
        val token = createToken(decimals = 3)
        val account = createAccount(token)

        Wait.waitAssert(timeout) {
            val records = findRecordByType(
                SubscriberGroup.TOKEN.collectionName,
                SolanaTokenRecord.InitializeTokenAccountRecord::class.java
            ).toList()

            assertEquals(1, records.size)
            val record = records.single()

            assertEquals(token, record.mint)
            assertEquals(wallet, record.owner)
            assertEquals(account, record.tokenAccount)
        }
    }

    @Test
    fun mintToken() = runBlocking {
        val decimals = 3
        val token = createToken(decimals)
        val account = createAccount(token)

        mintToken(token, 5UL)

        Wait.waitAssert(timeout) {
            val records = findRecordByType(
                collection = SubscriberGroup.TOKEN.collectionName,
                type = SolanaTokenRecord.MintToRecord::class.java
            ).toList()

            assertEquals(1, records.size)
            val record = records.single()

            assertEquals(token, record.mint)
            assertEquals(5 * 10.0.pow(decimals).toLong(), record.mintAmount)
            assertEquals(account, record.tokenAccount)
        }
    }

    @Test
    fun burnToken() = runBlocking {
        val decimals = 3
        val token = createToken(decimals)
        val account = createAccount(token)

        mintToken(token, 5UL)
        burnToken(account, 4UL)

        Wait.waitAssert(timeout) {
            val records = findRecordByType(
                collection = SubscriberGroup.TOKEN.collectionName,
                type = SolanaTokenRecord.BurnRecord::class.java
            ).toList()

            assertEquals(1, records.size)
            val record = records.single()

            assertEquals(token, record.mint)
            assertEquals(4 * 10.0.pow(decimals).toLong(), record.burnAmount)
            assertEquals(account, record.tokenAccount)
        }
    }

    @Test
    fun transferToken() = runBlocking {
        val aliceWallet = createWallet("${UUID.randomUUID()}")
        val token = createToken(0)
        val account = createAccount(token)
        val aliceAccount = createAccount(token, aliceWallet)

        mintToken(token, 1UL)
        transferToken(token, 1UL, aliceAccount)

        Wait.waitAssert(timeout) {
            val records = findRecordByType(
                collection = SubscriberGroup.BALANCE.collectionName,
                type = SolanaBalanceRecord.TransferIncomeRecord::class.java
            ).toList()

            assertEquals(1, records.size)
            val record = records.single()

            assertEquals(account, record.from)
            assertEquals(aliceAccount, record.to)
            assertEquals(1, record.incomeAmount)
        }

        Wait.waitAssert(timeout) {
            val records = findRecordByType(
                SubscriberGroup.BALANCE.collectionName,
                SolanaBalanceRecord.TransferOutcomeRecord::class.java
            ).toList()

            assertEquals(1, records.size)
            val record = records.single()

            assertEquals(account, record.from)
            assertEquals(aliceAccount, record.to)
            assertEquals(1, record.outcomeAmount)
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
