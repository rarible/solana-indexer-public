package com.rarible.protocol.solana.repository

import com.rarible.protocol.solana.AbstractIntegrationTest
import com.rarible.protocol.solana.common.repository.BalanceRepository
import com.rarible.protocol.solana.common.util.toBigInteger
import com.rarible.protocol.solana.test.createRandomBalance
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class BalanceRepositoryIt : AbstractIntegrationTest() {

    @Autowired
    private lateinit var balanceRepository: BalanceRepository

    @Test
    fun `save and find by account`() = runBlocking<Unit> {
        val balance = createRandomBalance()
        balanceRepository.save(balance)
        assertThat(balanceRepository.findByAccount(balance.account)).isEqualTo(balance)
    }

    @Test
    fun `save and find by owner`() = runBlocking<Unit> {
        val balance = createRandomBalance()
        val balance2 = createRandomBalance().copy(mint = balance.mint, owner = balance.owner)
        val balance3 = createRandomBalance().copy(mint = balance.mint)
        balanceRepository.save(balance)
        balanceRepository.save(balance2)
        balanceRepository.save(balance3)
        assertThat(balanceRepository.findByOwner(balance.owner).toList())
            .isEqualTo(listOf(balance, balance2).sortedBy { it.account })
    }

    @Test
    fun `save and find by mint`() = runBlocking<Unit> {
        val balance = createRandomBalance()
        val balance2 = createRandomBalance().copy(mint = balance.mint, owner = balance.owner)
        val balance3 = createRandomBalance().copy(owner = balance.owner)
        balanceRepository.save(balance)
        balanceRepository.save(balance2)
        balanceRepository.save(balance3)
        assertThat(balanceRepository.findByMint(balance.mint).toList())
            .isEqualTo(listOf(balance, balance2).sortedBy { it.account })
    }

    @Test
    fun `save with max ULong value and find by account`() = runBlocking<Unit> {
        val balance = createRandomBalance().copy(value = ULong.MAX_VALUE.toBigInteger())
        balanceRepository.save(balance)
        assertThat(balanceRepository.findByAccount(balance.account)).isEqualTo(balance)
    }

}
