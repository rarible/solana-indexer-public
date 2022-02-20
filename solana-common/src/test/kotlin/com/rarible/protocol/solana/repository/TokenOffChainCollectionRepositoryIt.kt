package com.rarible.protocol.solana.repository

import com.rarible.protocol.solana.AbstractIntegrationTest
import com.rarible.protocol.solana.common.repository.TokenOffChainCollectionRepository
import com.rarible.protocol.solana.test.createRandomTokenOffChainCollection
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class TokenOffChainCollectionRepositoryIt : AbstractIntegrationTest() {

    @Autowired
    private lateinit var tokenOffChainCollectionRepository: TokenOffChainCollectionRepository

    @Test
    fun `save off-chain collection and find by off-chain collection hash`() = runBlocking<Unit> {
        val tokenOffChainCollection = createRandomTokenOffChainCollection()
        tokenOffChainCollectionRepository.save(tokenOffChainCollection)
        assertThat(tokenOffChainCollectionRepository.findByOffChainCollectionHash(tokenOffChainCollection.hash))
            .isEqualTo(tokenOffChainCollection)
    }
}
