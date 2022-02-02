package com.rarible.protocol.solana.nft.listener

import com.rarible.core.test.wait.Wait
import com.rarible.protocol.solana.common.converter.BalanceConverter
import com.rarible.protocol.solana.common.converter.TokenConverter
import com.rarible.protocol.solana.common.model.Balance
import com.rarible.protocol.solana.common.model.Token
import com.rarible.protocol.solana.common.repository.BalanceRepository
import com.rarible.protocol.solana.common.repository.TokenRepository
import com.rarible.solana.protocol.dto.BalanceUpdateEventDto
import com.rarible.solana.protocol.dto.TokenUpdateEventDto
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.*

class TokenTest : EventAwareBlockScannerTest() {
    @Autowired
    private lateinit var tokenRepository: TokenRepository

    @Autowired
    private lateinit var balanceRepository: BalanceRepository

    @Test
    fun `mint, burn, transfer token`() = runBlocking {
        val decimals = 3
        val aliceWallet = createWallet("${UUID.randomUUID()}")
        val tokenAddress = createToken(decimals)
        val account = createAccount(tokenAddress)
        val aliceAccount = createAccount(tokenAddress, aliceWallet)

        mintToken(tokenAddress, amount = 5UL)
        val token = Token(
            mint = tokenAddress,
            collection = null,
            supply = 5.scaleSupply(decimals),
            isDeleted = false,
            revertableEvents = emptyList()
        )
        val fromBalance = Balance(
            account = account,
            value = 5.scaleSupply(decimals),
            revertableEvents = emptyList()
        )
        val aliceBalance = Balance(
            account = aliceAccount,
            value = 0,
            revertableEvents = emptyList()
        )

        Wait.waitAssert {
            assertToken(token)
            assertUpdateTokenEvent(token)
            assertUpdateBalanceEvent(fromBalance)
            assertBalance(fromBalance)
        }

        burnToken(account, amount = 4UL)
        Wait.waitAssert {
            val partlyBurnedToken = token.copy(supply = 1.scaleSupply(decimals))
            assertToken(partlyBurnedToken)
            assertUpdateTokenEvent(partlyBurnedToken)

            val partlyBurnedBalance = fromBalance.copy(value = 1.scaleSupply(decimals))
            assertBalance(partlyBurnedBalance)
            assertUpdateBalanceEvent(partlyBurnedBalance)
        }

        transferToken(tokenAddress, amount = 1UL, aliceAccount)
        Wait.waitAssert {
            val finalToken = token.copy(supply = 1.scaleSupply(decimals))
            assertToken(finalToken)
            assertUpdateTokenEvent(finalToken)

            val finalFromBalance = fromBalance.copy(value = 0.scaleSupply(decimals))
            assertBalance(finalFromBalance)
            assertUpdateBalanceEvent(finalFromBalance)

            val finalAliceBalance = aliceBalance.copy(value = 1.scaleSupply(decimals))
            assertBalance(finalAliceBalance)
            assertUpdateBalanceEvent(finalAliceBalance)
        }
    }

    private fun assertUpdateTokenEvent(token: Token) {
        assertThat(tokenEvents).anySatisfy { event ->
            assertThat(event).isInstanceOfSatisfying(TokenUpdateEventDto::class.java) {
                assertThat(it.address).isEqualTo(token.mint)
                assertThat(it.token).isEqualTo(TokenConverter.convert(token))
            }
        }
    }

    private fun assertUpdateBalanceEvent(balance: Balance) {
        assertThat(balanceEvents).anySatisfy { event ->
            assertThat(event).isInstanceOfSatisfying(BalanceUpdateEventDto::class.java) {
                assertThat(it.account).isEqualTo(balance.account)
                assertThat(it.balance).isEqualTo(BalanceConverter.convert(balance))
            }
        }
    }

    private suspend fun assertToken(expectedToken: Token) {
        val actualToken = tokenRepository.findById(expectedToken.mint)
        assertTokensEqual(actualToken, expectedToken)
    }

    private fun assertTokensEqual(actualToken: Token?, expectedToken: Token) {
        // Ignore [revertableEvents] because they are minor and hard to get (transaction hashes change all the time)
        assertThat(actualToken?.copy(revertableEvents = emptyList()))
            .isEqualTo(expectedToken.copy(revertableEvents = emptyList()))
    }

    private suspend fun assertBalance(balance: Balance) {
        // Ignore [revertableEvents] because they are minor and hard to get (transaction hashes change all the time)
        assertThat(balanceRepository.findById(balance.account)?.copy(revertableEvents = emptyList()))
            .isEqualTo(balance.copy(revertableEvents = emptyList()))
    }

}
