package com.rarible.protocol.solana.nft.api.controller

import com.rarible.protocol.solana.common.converter.BalanceConverter
import com.rarible.protocol.solana.nft.api.test.AbstractControllerTest
import com.rarible.protocol.solana.test.createRandomBalance
import io.mockk.coEvery
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class BalanceControllerIt : AbstractControllerTest() {

    @Test
    fun `find balance by account`() = runBlocking<Unit> {
        val balance = createRandomBalance()
        coEvery { testBalanceService.getBalance(balance.account) } returns balance
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
        coEvery { testBalanceService.getBalanceByOwner(balance.owner) } returns flowOf(balance, balance2)
        assertThat(balanceControllerApi.getBalanceByOwner(balance.owner).awaitFirst())
            .isEqualTo(BalanceConverter.convert(listOf(balance, balance2)))
    }

}
