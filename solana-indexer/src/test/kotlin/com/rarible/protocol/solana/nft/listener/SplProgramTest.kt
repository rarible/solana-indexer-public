package com.rarible.protocol.solana.nft.listener

import com.rarible.blockchain.scanner.solana.model.SolanaLogRecord
import com.rarible.core.test.wait.Wait
import com.rarible.protocol.solana.borsh.MetaplexCreateMetadataAccountArgs
import com.rarible.protocol.solana.borsh.MetaplexMetadataProgram
import com.rarible.protocol.solana.nft.listener.service.records.SolanaBalanceRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaMetaRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaTokenRecord
import com.rarible.protocol.solana.nft.listener.service.subscribers.SubscriberGroup
import com.rarible.protocol.solana.test.ANY_SOLANA_LOG
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.isEqualTo
import java.math.BigInteger
import java.time.Duration
import java.time.Instant
import java.util.*

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
                type = SolanaMetaRecord.MetaplexCreateMetadataAccountRecord::class.java
            ).toList()

            assertThat(records).usingElementComparatorIgnoringFields(
                SolanaMetaRecord.MetaplexCreateMetadataAccountRecord::log.name,
                SolanaMetaRecord.MetaplexCreateMetadataAccountRecord::metaAccount.name,
                SolanaMetaRecord.MetaplexCreateMetadataAccountRecord::timestamp.name
            ).isEqualTo(
                listOf(
                    SolanaMetaRecord.MetaplexCreateMetadataAccountRecord(
                        mint = nft,
                        data = MetaplexCreateMetadataAccountArgs(
                            mutable = false,
                            metadata = MetaplexMetadataProgram.Data(
                                name = "My NFT #1",
                                uri = "https://gist.githubusercontent.com/enslinmike/a18bd9fa8e922d641a8a8a64ce84dea6/raw/a8298b26e47f30279a1b107f19287be4f198e21d/meta.json",
                                symbol = "MY_SYMBOL",
                                sellerFeeBasisPoints = 420,
                                creators = listOf(
                                    MetaplexMetadataProgram.Creator(
                                        address = wallet,
                                        share = 100,
                                        verified = true
                                    )
                                ),
                                collection = null
                            )
                        ),
                        metaAccount = "", // TODO: we can calculate the PDA of metadata account.
                        log = ANY_SOLANA_LOG,
                        timestamp = Instant.EPOCH
                    )
                )
            )
        }
    }

    @Test
    fun `verify collection`() = runBlocking {
        val collection = mintNft()
        val nft = mintNft(collection)

        Wait.waitAssert(timeout) {
            val meta = metaplexMetaRepository.findByTokenAddress(nft) ?: fail("Meta not ready")
            assertThat(meta.metaFields.collection?.verified).isFalse
        }

        verifyCollection(nft, collection)

        Wait.waitAssert(timeout) {
            val meta = metaplexMetaRepository.findByTokenAddress(nft) ?: fail("Meta not ready")
            assertThat(meta.metaFields.collection?.verified).isTrue
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

            assertThat(records).usingElementComparatorIgnoringFields(
                SolanaTokenRecord.InitializeMintRecord::log.name,
                SolanaTokenRecord.InitializeMintRecord::timestamp.name
            ).isEqualTo(
                listOf(
                    SolanaTokenRecord.InitializeMintRecord(
                        mint = token,
                        mintAuthority = wallet,
                        decimals = 3,
                        log = ANY_SOLANA_LOG,
                        timestamp = Instant.EPOCH
                    )
                )
            )
        }
    }

    @Test
    fun createAccount() = runBlocking {
        val wallet = getWallet()
        val token = createToken(decimals = 3)
        val account = createAccount(token)

        Wait.waitAssert(timeout) {
            val records = findRecordByType(
                SubscriberGroup.BALANCE.collectionName,
                SolanaBalanceRecord.InitializeBalanceAccountRecord::class.java
            ).toList()

            assertThat(records).usingElementComparatorIgnoringFields(
                SolanaBalanceRecord.InitializeBalanceAccountRecord::log.name,
                SolanaBalanceRecord.InitializeBalanceAccountRecord::timestamp.name
            ).isEqualTo(
                listOf(
                    SolanaBalanceRecord.InitializeBalanceAccountRecord(
                        mint = token,
                        owner = wallet,
                        balanceAccount = account,
                        log = ANY_SOLANA_LOG,
                        timestamp = Instant.EPOCH
                    )
                )
            )
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

            assertThat(records).usingElementComparatorIgnoringFields(
                SolanaTokenRecord.MintToRecord::log.name,
                SolanaTokenRecord.MintToRecord::timestamp.name
            ).isEqualTo(
                listOf(
                    SolanaTokenRecord.MintToRecord(
                        mint = token,
                        mintAmount = 5.scaleSupply(decimals),
                        tokenAccount = account,
                        log = ANY_SOLANA_LOG,
                        timestamp = Instant.EPOCH
                    )
                )
            )
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

            assertThat(records).usingElementComparatorIgnoringFields(
                SolanaTokenRecord.BurnRecord::log.name,
                SolanaTokenRecord.BurnRecord::timestamp.name
            ).isEqualTo(
                listOf(
                    SolanaTokenRecord.BurnRecord(
                        mint = token,
                        burnAmount = 4.scaleSupply(decimals),
                        tokenAccount = account,
                        log = ANY_SOLANA_LOG,
                        timestamp = Instant.EPOCH
                    )
                )
            )
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

            assertThat(records).usingElementComparatorIgnoringFields(
                SolanaBalanceRecord.TransferIncomeRecord::log.name,
                SolanaBalanceRecord.TransferIncomeRecord::timestamp.name
            ).isEqualTo(
                listOf(
                    SolanaBalanceRecord.TransferIncomeRecord(
                        from = account,
                        to = aliceAccount,
                        incomeAmount = BigInteger.ONE,
                        log = ANY_SOLANA_LOG,
                        timestamp = Instant.EPOCH
                    )
                )
            )
        }

        Wait.waitAssert(timeout) {
            val records = findRecordByType(
                SubscriberGroup.BALANCE.collectionName,
                SolanaBalanceRecord.TransferOutcomeRecord::class.java
            ).toList()

            assertThat(records).usingElementComparatorIgnoringFields(
                SolanaBalanceRecord.TransferOutcomeRecord::log.name,
                SolanaBalanceRecord.TransferOutcomeRecord::timestamp.name
            ).isEqualTo(
                listOf(
                    SolanaBalanceRecord.TransferOutcomeRecord(
                        from = account,
                        to = aliceAccount,
                        outcomeAmount = BigInteger.ONE,
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
