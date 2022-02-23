package com.rarible.protocol.solana.nft.api.service

import com.rarible.protocol.solana.common.model.BalanceWithMeta
import com.rarible.protocol.solana.common.repository.BalanceRepository
import com.rarible.protocol.solana.test.createRandomBalance
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class BalanceApiServiceIt : AbstractMetaAwareIntegrationTest() {

    @Autowired
    private lateinit var balanceRepository: BalanceRepository

    @Autowired
    private lateinit var balanceApiService: BalanceApiService

    @Test
    fun `find balance with meta by account address`() = runBlocking<Unit> {
        val balance = createRandomBalance()
        balanceRepository.save(balance)
        val balanceMeta = saveRandomMetaplexOnChainAndOffChainMeta(balance.mint)
        assertThat(balanceApiService.getBalanceWithMetaByAccountAddress(balance.account))
            .isEqualTo(BalanceWithMeta(balance, balanceMeta))
    }

    @Test
    fun `find balances with meta by owner`() = runBlocking<Unit> {
        val balance = createRandomBalance()
        val balance2 = createRandomBalance().copy(owner = balance.owner)
        val balance3 = createRandomBalance()
        balanceRepository.save(balance)
        balanceRepository.save(balance2)
        balanceRepository.save(balance3)

        val tokenMeta = saveRandomMetaplexOnChainAndOffChainMeta(tokenAddress = balance.mint)
        val tokenMeta2 = saveRandomMetaplexOnChainAndOffChainMeta(tokenAddress = balance2.mint)
        assertThat(balanceApiService.getBalanceWithMetaByOwner(balance.owner).toList())
            .isEqualTo(
                listOf(
                    BalanceWithMeta(balance, tokenMeta),
                    BalanceWithMeta(balance2, tokenMeta2)
                ).sortedBy { it.balance.account }
            )
    }

    @Test
    fun `find balances with meta by mint`() = runBlocking<Unit> {
        val balance = createRandomBalance()
        val balance2 = createRandomBalance().copy(mint = balance.mint)
        val balance3 = createRandomBalance()
        balanceRepository.save(balance)
        balanceRepository.save(balance2)
        balanceRepository.save(balance3)

        val tokenMeta = saveRandomMetaplexOnChainAndOffChainMeta(tokenAddress = balance.mint)
        assertThat(balanceApiService.getBalanceWithMetaByMint(balance.mint).toList())
            .isEqualTo(
                listOf(
                    BalanceWithMeta(balance, tokenMeta),
                    BalanceWithMeta(balance2, tokenMeta)
                ).sortedBy { it.balance.account }
            )
    }

}
