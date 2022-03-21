package com.rarible.protocol.solana.nft.listener.repository

import com.rarible.core.test.data.randomString
import com.rarible.protocol.solana.nft.listener.AbstractBlockScannerTest
import com.rarible.protocol.solana.nft.listener.model.AccountToMintAssociation
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class AccountToMintAssociationRepositoryIt : AbstractBlockScannerTest() {

    @Autowired
    lateinit var repository: AccountToMintAssociationRepository

    @Test
    fun `save - new records`() = runBlocking<Unit> {
        val association1 = AccountToMintAssociation(randomString(), randomString())
        val association2 = AccountToMintAssociation(randomString(), randomString())

        repository.saveAll(listOf(association1, association2))

        val mapping = repository.findAll(listOf(association1.balanceAccount, association2.balanceAccount))
            .associateBy { it.balanceAccount }

        assertThat(mapping).hasSize(2)
        assertThat(mapping[association1.balanceAccount]).isEqualTo(association1)
        assertThat(mapping[association2.balanceAccount]).isEqualTo(association2)
    }

    @Test
    fun `save - duplicate records`() = runBlocking<Unit> {
        val association1 = AccountToMintAssociation(randomString(), randomString())
        val association2 = AccountToMintAssociation(randomString(), randomString())
        val association3 = AccountToMintAssociation(randomString(), randomString())

        repository.saveAll(listOf(association1, association2))
        // Saving existing and new association
        repository.saveAll(listOf(association1, association3))

        val mapping = repository.findAll(
            listOf(
                association1.balanceAccount,
                association2.balanceAccount,
                association3.balanceAccount
            )
        ).associateBy { it.balanceAccount }

        assertThat(mapping).hasSize(3)
        assertThat(mapping[association1.balanceAccount]).isEqualTo(association1)
        assertThat(mapping[association2.balanceAccount]).isEqualTo(association2)
        assertThat(mapping[association3.balanceAccount]).isEqualTo(association3)
    }

}