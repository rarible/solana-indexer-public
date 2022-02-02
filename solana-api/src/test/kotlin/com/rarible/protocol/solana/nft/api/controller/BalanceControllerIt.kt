package com.rarible.protocol.solana.nft.api.controller

import com.rarible.protocol.solana.common.converter.BalanceConverter
import com.rarible.protocol.solana.common.converter.TokenConverter
import com.rarible.protocol.solana.common.repository.BalanceRepository
import com.rarible.protocol.solana.nft.api.data.createRandomBalance
import com.rarible.protocol.solana.nft.api.test.AbstractIntegrationTest
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
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
}
