package com.rarible.protocol.solana.nft.listener.service

import com.rarible.core.test.data.randomString
import com.rarible.protocol.solana.nft.listener.AbstractBlockScannerTest
import com.rarible.protocol.solana.nft.listener.model.AccountToMintAssociation
import com.rarible.protocol.solana.nft.listener.repository.AccountToMintAssociationRepository
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class AccountToMintAssociationServiceIt : AbstractBlockScannerTest() {

    @Autowired
    lateinit var accountToMintAssociationService: AccountToMintAssociationService

    @Autowired
    lateinit var accountToMintAssociationRepository: AccountToMintAssociationRepository

    @Test
    fun `save and get mapping`() = runBlocking<Unit> {
        val account1 = randomString()
        val account2 = randomString()
        val account3 = randomString()

        accountToMintAssociationService.saveMintsByAccounts(mapOf(account1 to "1", account2 to "2"))

        val cached = accountToMintAssociationService.getMintsByAccounts(listOf(account1, account2, account3))

        assertThat(cached[account1]).isEqualTo("1")
        assertThat(cached[account2]).isEqualTo("2")
        assertThat(cached[account3]).isNull()
    }

    @Test
    fun `get mapping`() = runBlocking<Unit> {
        val accountCached = randomString()
        val accountDb = randomString()
        val accountNotFound = randomString()

        val dbAssociation = AccountToMintAssociation(account = accountDb, mint = "2")

        accountToMintAssociationRepository.saveAll(listOf(dbAssociation))
        accountToMintAssociationService.saveMintsByAccounts(mapOf(accountCached to "1"))

        val mints = accountToMintAssociationService.getMintsByAccounts(
            listOf(accountCached, accountDb, accountNotFound)
        )

        assertThat(mints[accountCached]).isEqualTo("1")
        assertThat(mints[accountDb]).isEqualTo("2")
        assertThat(mints[accountNotFound]).isNull()
    }

}
