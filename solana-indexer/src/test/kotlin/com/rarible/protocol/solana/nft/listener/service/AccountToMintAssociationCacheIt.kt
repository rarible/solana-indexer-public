package com.rarible.protocol.solana.nft.listener.service

import com.rarible.protocol.solana.nft.listener.AbstractBlockScannerTest
import com.rarible.protocol.solana.test.randomAccount
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class AccountToMintAssociationCacheIt : AbstractBlockScannerTest() {

    @Autowired
    lateinit var cache: AccountToMintAssociationCache

    @Test
    fun `get mapping - empty map`() = runBlocking<Unit> {
        // Should not throw an exception
        val mapping = cache.getMintsByAccounts(emptyList())
        assertThat(mapping).isEmpty()
    }

    @Test
    fun `save and get mapping`() = runBlocking<Unit> {
        val account1 = randomAccount()
        val account2 = randomAccount()
        val account3 = randomAccount()

        cache.saveMintsByAccounts(mapOf(account1 to "1", account2 to "2"))

        val cached = cache.getMintsByAccounts(listOf(account1, account2, account3))

        assertThat(cached[account1]).isEqualTo("1")
        assertThat(cached[account2]).isEqualTo("2")
        assertThat(cached[account3]).isNull()
    }

    @Test
    fun `save mapping - empty map`() = runBlocking<Unit> {
        // Should not throw an exception
        cache.saveMintsByAccounts(emptyMap())
    }

}