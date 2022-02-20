package com.rarible.protocol.solana.nft.api.controller

import com.rarible.protocol.solana.common.converter.BalanceConverter
import com.rarible.protocol.solana.common.repository.BalanceRepository
import com.rarible.protocol.solana.test.createRandomBalance
import com.rarible.protocol.solana.nft.api.test.AbstractIntegrationTest
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class BalanceControllerIt : AbstractIntegrationTest() {

    @Autowired
    private lateinit var balanceRepository: BalanceRepository

    @Test
    fun `find balance by account`() = runBlocking<Unit> {
        val balance = createRandomBalance()
        balanceRepository.save(balance)
        assertThat(balanceControllerApi.getBalanceByAccount(balance.account).awaitFirst())
            .isEqualTo(BalanceConverter.convert(balance))
    }

    @Test
    fun `find balances by owner`() = runBlocking<Unit> {
        val balance = createRandomBalance()
        val balance2 = createRandomBalance().copy(
            mint = balance.mint,
            owner = balance.owner
        )
        val balance3 = createRandomBalance().copy(mint = balance.mint)
        balanceRepository.save(balance)
        balanceRepository.save(balance2)
        balanceRepository.save(balance3)
        assertThat(balanceControllerApi.getBalanceByOwner(balance.owner).awaitFirst())
            .isEqualTo(BalanceConverter.convert(listOf(balance, balance2).sortedBy { it.account }))
    }

}
