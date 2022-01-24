package com.rarible.protocol.solana.nft.listener

import com.rarible.blockchain.scanner.solana.model.SolanaLogRecord
import com.rarible.core.test.wait.Wait
import com.rarible.protocol.solana.nft.listener.service.records.SolanaItemLogRecord.BurnRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaItemLogRecord.InitializeAccountRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaItemLogRecord.InitializeMintRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaItemLogRecord.MintToRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaItemLogRecord.TransferRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.isEqualTo
import java.time.Duration
import kotlin.math.pow

@Disabled
class SplProgramTest : AbstractBlockScannerTest() {
    private val timeout = Duration.ofSeconds(5)

    @Autowired
    private lateinit var mongo: ReactiveMongoOperations

    @Test
    fun initializeMint() = runBlocking {
        val wallet = getWallet()
        val token = createToken(decimals = 3)

        Wait.waitAssert(timeout) {
            val records = findRecordByType(InitializeMintRecord::class.java).toList()

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
            val records = findRecordByType(InitializeAccountRecord::class.java).toList()

            assertEquals(1, records.size)
            val record = records.single()

            assertEquals(token, record.mint)
            assertEquals(wallet, record.owner)
            assertEquals(account, record.account)
        }
    }

    @Test
    fun mintToken() = runBlocking {
        val decimals = 3
        val token = createToken(decimals)
        val account = createAccount(token)

        mintToken(token, 5UL)

        Wait.waitAssert(timeout) {
            val records = findRecordByType(MintToRecord::class.java).toList()

            assertEquals(1, records.size)
            val record = records.single()

            assertEquals(token, record.mint)
            assertEquals(5 * 10.0.pow(decimals).toLong(), record.mintAmount)
            assertEquals(account, record.account)
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
            val records = findRecordByType(BurnRecord::class.java).toList()

            assertEquals(1, records.size)
            val record = records.single()

            assertEquals(token, record.mint)
            assertEquals(4 * 10.0.pow(decimals).toLong(), record.burnAmount)
            assertEquals(account, record.account)
        }
    }

    @Test
    fun transferToken() = runBlocking {
        val aliceWallet = createWallet("alice")
        val token = createToken(0)
        val account = createAccount(token)
        val aliceAccount = createAccount(token, aliceWallet)

        mintToken(token, 1UL)
        transferToken(token, 1UL, aliceAccount)

        Wait.waitAssert(timeout) {
            val records = findRecordByType(TransferRecord::class.java).toList()

            assertEquals(1, records.size)
            val record = records.single()

            assertEquals(token, record.mint)
            assertEquals(account, record.from)
            assertEquals(aliceAccount, record.to)
            assertEquals(1, record.amount)
        }
    }

    private inline fun <reified T : SolanaLogRecord> findRecordByType(type: Class<T>): Flow<T> {
        val criteria = Criteria.where("_class").isEqualTo(T::class.java.name)

        return mongo.find(Query(criteria), type, "spl-token").asFlow()
    }
}