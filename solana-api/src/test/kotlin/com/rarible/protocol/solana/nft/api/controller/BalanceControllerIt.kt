package com.rarible.protocol.solana.nft.api.controller

import com.rarible.protocol.solana.common.converter.BalanceWithMetaConverter
import com.rarible.protocol.solana.nft.api.test.AbstractControllerTest
import com.rarible.protocol.solana.test.createRandomBalanceWithMeta
import io.mockk.coEvery
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class BalanceControllerIt : AbstractControllerTest() {

    @Test
    fun `find balance by account`() = runBlocking<Unit> {
        val balanceWithMeta = createRandomBalanceWithMeta()
        coEvery { testBalanceApiService.getBalanceWithMetaByAccountAddress(balanceWithMeta.balance.account) } returns balanceWithMeta
        assertThat(balanceControllerApi.getBalanceByAccount(balanceWithMeta.balance.account).awaitFirst())
            .isEqualTo(BalanceWithMetaConverter.convert(balanceWithMeta))
    }

    @Test
    fun `find balances by owner`() = runBlocking<Unit> {
        val balanceWithMeta = createRandomBalanceWithMeta()
        val balance = balanceWithMeta.balance
        val balanceWithMeta2 = createRandomBalanceWithMeta().let {
            it.copy(
                balance = it.balance.copy(
                    mint = balance.mint,
                    owner = balance.owner
                )
            )
        }
        coEvery { testBalanceApiService.getBalanceWithMetaByOwner(balance.owner) } returns flowOf(
            balanceWithMeta,
            balanceWithMeta2
        )
        assertThat(balanceControllerApi.getBalanceByOwner(balance.owner).awaitFirst())
            .isEqualTo(
                BalanceWithMetaConverter.convert(
                    listOf(balanceWithMeta, balanceWithMeta2)
                )
            )
    }

}
