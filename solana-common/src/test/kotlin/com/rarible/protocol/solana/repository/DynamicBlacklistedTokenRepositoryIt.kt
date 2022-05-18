package com.rarible.protocol.solana.repository

import com.rarible.core.test.data.randomString
import com.rarible.protocol.solana.AbstractIntegrationTest
import com.rarible.protocol.solana.common.repository.DynamicBlacklistedTokenRepository
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class DynamicBlacklistedTokenRepositoryIt : AbstractIntegrationTest() {
    @Autowired
    private lateinit var dynamicBlacklistedTokenRepository: DynamicBlacklistedTokenRepository

    @Test
    fun `save and find by mint`() = runBlocking<Unit> {
        val mint = randomString()
        dynamicBlacklistedTokenRepository.save(mint, randomString())
        assertThat(dynamicBlacklistedTokenRepository.findAll(listOf(mint))).isEqualTo(setOf(mint))
    }

    @Test
    fun `save many and find by mints`() = runBlocking<Unit> {
        val mints = (0 until 100).map { randomString() }
        dynamicBlacklistedTokenRepository.saveAll(mints, randomString())
        val otherMints = (0 until 50).map { randomString() }
        val savedMints = mints.take(50)
        val request = savedMints + otherMints
        val response = dynamicBlacklistedTokenRepository.findAll(request.shuffled())
        assertThat(response).isEqualTo(savedMints.toSet())
    }
}