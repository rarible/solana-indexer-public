package com.rarible.protocol.solana.nft.listener.service

import com.rarible.core.test.data.randomString
import com.rarible.protocol.solana.nft.listener.AbstractBlockScannerTest
import com.rarible.protocol.solana.nft.listener.test.data.randomBalanceInitRecord
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class AccountToMintAssociationServiceIt : AbstractBlockScannerTest() {

    @Autowired
    lateinit var accountToMintAssociationService: AccountToMintAssociationService

    @Test
    fun `save mapping`() = runBlocking<Unit> {
        val account1 = randomString()
        val account2 = randomString()
        val account3 = randomString()
        accountToMintAssociationService.saveAccountToMintMapping(mapOf(account1 to "1", account2 to "2"))

        val first = accountToMintAssociationService.getMintByAccount(account1)
        val second = accountToMintAssociationService.getMintByAccount(account2)
        val notFound = accountToMintAssociationService.getMintByAccount(account3)

        assertThat(first).isEqualTo("1")
        assertThat(second).isEqualTo("2")
        assertThat(notFound).isNull()
    }

    @Test
    fun `save mapping - empty map`() = runBlocking<Unit> {
        // Should not throw an exception
        accountToMintAssociationService.saveAccountToMintMapping(emptyMap())
    }

    @Test
    fun `get mapping`() = runBlocking<Unit> {
        val accountCached = randomString()
        val accountDb = randomString()
        val accountNotFound = randomString()

        saveBalanceRecord(randomBalanceInitRecord().copy(balanceAccount = accountDb, mint = "2"))
        accountToMintAssociationService.saveAccountToMintMapping(mapOf(accountCached to "1"))

        val mints = accountToMintAssociationService.getMintsByAccounts(
            listOf(accountCached, accountDb, accountNotFound)
        )

        assertThat(mints[accountCached]).isEqualTo("1")
        assertThat(mints[accountDb]).isEqualTo("2")
        assertThat(mints[accountNotFound]).isNull()
    }

    @Test
    fun `get mapping - empty map`() = runBlocking<Unit> {
        val result = accountToMintAssociationService.getMintsByAccounts(emptyList())

        assertThat(result).hasSize(0)
    }

}