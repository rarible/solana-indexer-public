package com.rarible.protocol.solana.nft.listener

import com.rarible.core.test.wait.Wait
import com.rarible.protocol.solana.common.records.EMPTY_SOLANA_LOG
import com.rarible.protocol.solana.common.records.SolanaBalanceRecord
import com.rarible.protocol.solana.common.records.SolanaTokenRecord
import com.rarible.protocol.solana.common.records.SubscriberGroup
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigInteger
import java.time.Duration
import java.time.Instant
import java.util.*

class SplProgramTest : AbstractBlockScannerTest() {
    private val timeout = Duration.ofSeconds(10)

    @Test
    fun initializeMint() = runBlocking {
        val wallet = getWallet()
        val token = createToken(decimals = 3)

        Wait.waitAssert(timeout) {
            val records = findRecordByType(
                SubscriberGroup.TOKEN.collectionName,
                SolanaTokenRecord.InitializeMintRecord::class.java
            ).toList()

            assertThat(records).usingRecursiveFieldByFieldElementComparatorIgnoringFields(
                SolanaTokenRecord.InitializeMintRecord::log.name,
                SolanaTokenRecord.InitializeMintRecord::timestamp.name
            ).isEqualTo(
                listOf(
                    SolanaTokenRecord.InitializeMintRecord(
                        mint = token,
                        mintAuthority = wallet,
                        decimals = 3,
                        log = EMPTY_SOLANA_LOG,
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

            assertThat(records).anySatisfy { record ->
                assertThat(record).usingRecursiveComparison().ignoringFields(
                    SolanaBalanceRecord.InitializeBalanceAccountRecord::log.name,
                    SolanaBalanceRecord.InitializeBalanceAccountRecord::timestamp.name
                ).isEqualTo(
                    SolanaBalanceRecord.InitializeBalanceAccountRecord(
                        mint = token,
                        owner = wallet,
                        account = account,
                        log = EMPTY_SOLANA_LOG,
                        timestamp = Instant.EPOCH
                    )
                )
            }
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

            assertThat(records).usingRecursiveFieldByFieldElementComparatorIgnoringFields(
                SolanaTokenRecord.MintToRecord::log.name,
                SolanaTokenRecord.MintToRecord::timestamp.name
            ).isEqualTo(
                listOf(
                    SolanaTokenRecord.MintToRecord(
                        mint = token,
                        mintAmount = 5.scaleSupply(decimals),
                        tokenAccount = account,
                        log = EMPTY_SOLANA_LOG,
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

            assertThat(records).usingRecursiveFieldByFieldElementComparatorIgnoringFields(
                SolanaTokenRecord.BurnRecord::log.name,
                SolanaTokenRecord.BurnRecord::timestamp.name
            ).isEqualTo(
                listOf(
                    SolanaTokenRecord.BurnRecord(
                        mint = token,
                        burnAmount = 4.scaleSupply(decimals),
                        tokenAccount = account,
                        log = EMPTY_SOLANA_LOG,
                        timestamp = Instant.EPOCH
                    )
                )
            )
        }
    }

    @Test
    fun changeOwner() = runBlocking {
        val decimals = 3
        val token = createToken(decimals)
        val keypair = createKeypair("${UUID.randomUUID()}")
        val secondary = createAccount(token, keypair = keypair)
        val wallet = getWallet(baseKeypair)
        val aliceWallet = createWallet("${UUID.randomUUID()}")

        Wait.waitAssert(timeout) {
            val records = findRecordByType(
                SubscriberGroup.BALANCE.collectionName,
                SolanaBalanceRecord.InitializeBalanceAccountRecord::class.java
            ).toList()

            assertThat(records).anySatisfy { record ->
                assertThat(record).usingRecursiveComparison().ignoringFields(
                    SolanaBalanceRecord.InitializeBalanceAccountRecord::log.name,
                    SolanaBalanceRecord.InitializeBalanceAccountRecord::timestamp.name
                ).isEqualTo(
                    SolanaBalanceRecord.InitializeBalanceAccountRecord(
                        mint = token,
                        owner = wallet,
                        account = secondary,
                        log = EMPTY_SOLANA_LOG,
                        timestamp = Instant.EPOCH
                    )
                )
            }
        }

        changeOwner(secondary, aliceWallet)

        Wait.waitAssert(timeout) {
            val records = findRecordByType(
                SubscriberGroup.BALANCE.collectionName,
                SolanaBalanceRecord.ChangeOwnerRecord::class.java
            ).toList()

            assertThat(records).anySatisfy { record ->
                assertThat(record).usingRecursiveComparison().ignoringFields(
                    SolanaBalanceRecord.ChangeOwnerRecord::log.name,
                    SolanaBalanceRecord.ChangeOwnerRecord::timestamp.name
                ).isEqualTo(
                    SolanaBalanceRecord.ChangeOwnerRecord(
                        mint = token,
                        oldOwner = wallet,
                        newOwner = aliceWallet,
                        account = secondary,
                        log = EMPTY_SOLANA_LOG,
                        timestamp = Instant.EPOCH
                    )
                )
            }

            assertThat(balanceRepository.findByAccount(secondary)?.owner).isEqualTo(aliceWallet)
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

            assertThat(records).usingRecursiveFieldByFieldElementComparatorIgnoringFields(
                SolanaBalanceRecord.TransferIncomeRecord::log.name,
                SolanaBalanceRecord.TransferIncomeRecord::timestamp.name
            ).isEqualTo(
                listOf(
                    SolanaBalanceRecord.TransferIncomeRecord(
                        from = account,
                        account = aliceAccount,
                        incomeAmount = BigInteger.ONE,
                        log = EMPTY_SOLANA_LOG,
                        timestamp = Instant.EPOCH,
                        mint = token
                    )
                )
            )
        }

        Wait.waitAssert(timeout) {
            val records = findRecordByType(
                SubscriberGroup.BALANCE.collectionName,
                SolanaBalanceRecord.TransferOutcomeRecord::class.java
            ).toList()

            assertThat(records).usingRecursiveFieldByFieldElementComparatorIgnoringFields(
                SolanaBalanceRecord.TransferOutcomeRecord::log.name,
                SolanaBalanceRecord.TransferOutcomeRecord::timestamp.name
            ).isEqualTo(
                listOf(
                    SolanaBalanceRecord.TransferOutcomeRecord(
                        to = aliceAccount,
                        account = account,
                        mint = token,
                        outcomeAmount = BigInteger.ONE,
                        log = EMPTY_SOLANA_LOG,
                        timestamp = Instant.EPOCH
                    )
                )
            )
        }
    }
}
